package com.dwp.employeecatalog.util;

import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * JUnit Platform listener that records a timestamped one-line summary of every
 * test run and keeps a rolling history of the most recent {@value #MAX_RUNS}
 * runs in {@code test-history/test-results.log}.
 *
 * <p><b>Purpose:</b> give a quick, human-readable audit trail of how the suite
 * has behaved over its last few executions (date/time, counts, pass/fail),
 * which is handy against a flaky remote host where the skipped/aborted count
 * varies run to run.</p>
 *
 * <p>Registered automatically via
 * {@code META-INF/services/org.junit.platform.launcher.TestExecutionListener},
 * so it runs on every {@code mvn test} with no wiring in the tests themselves.
 * The history file lives at the project root (not under {@code target/}), so it
 * survives {@code mvn clean}.</p>
 */
public class TestRunHistoryListener implements TestExecutionListener {

    /** How many past runs to retain (older lines are dropped). */
    private static final int MAX_RUNS = 10;

    private static final Path LOG = Path.of("test-history", "test-results.log");
    private static final DateTimeFormatter TIMESTAMP =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final AtomicInteger passed = new AtomicInteger();
    private final AtomicInteger failed = new AtomicInteger();
    private final AtomicInteger skipped = new AtomicInteger(); // @Disabled + aborted/skipped
    private long startMillis;

    @Override
    public void testPlanExecutionStarted(TestPlan testPlan) {
        passed.set(0);
        failed.set(0);
        skipped.set(0);
        startMillis = System.currentTimeMillis();
    }

    @Override
    public void executionSkipped(TestIdentifier testIdentifier, String reason) {
        if (testIdentifier.isTest()) {
            skipped.incrementAndGet();
        }
    }

    @Override
    public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult result) {
        if (!testIdentifier.isTest()) {
            return; // ignore containers (classes); count only individual test methods
        }
        switch (result.getStatus()) {
            case SUCCESSFUL -> passed.incrementAndGet();
            case FAILED -> failed.incrementAndGet();
            case ABORTED -> skipped.incrementAndGet(); // e.g. TestAbortedException on host outage
        }
    }

    @Override
    public void testPlanExecutionFinished(TestPlan testPlan) {
        int p = passed.get();
        int f = failed.get();
        int s = skipped.get();
        int total = p + f + s;
        double seconds = (System.currentTimeMillis() - startMillis) / 1000.0;

        String line = String.format(
                "%s | duration=%6.1fs | total=%2d  passed=%2d  failed=%2d  skipped=%2d | RESULT=%s",
                LocalDateTime.now().format(TIMESTAMP), seconds, total, p, f, s,
                f == 0 ? "PASS" : "FAIL");

        appendKeepingLastRuns(line);
    }

    /** Appends the line, then trims the file to the last {@link #MAX_RUNS} lines. */
    private static synchronized void appendKeepingLastRuns(String line) {
        try {
            if (LOG.getParent() != null) {
                Files.createDirectories(LOG.getParent());
            }
            List<String> lines = Files.exists(LOG)
                    ? new ArrayList<>(Files.readAllLines(LOG))
                    : new ArrayList<>();
            lines.add(line);
            if (lines.size() > MAX_RUNS) {
                lines = new ArrayList<>(lines.subList(lines.size() - MAX_RUNS, lines.size()));
            }
            Files.write(LOG, lines);
        } catch (IOException e) {
            // Never let history logging break the build.
            System.err.println("[TestRunHistoryListener] could not write " + LOG + ": " + e.getMessage());
        }
    }
}
