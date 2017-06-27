package me.buom.junit;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * Created by buom on 1/14/14.
 */
public class RepeatRule implements TestRule {

    static class RepeatStatement extends Statement {
        private final int times;
        private final long warmUp;
        private final Statement statement;

        private RepeatStatement(int times, long warmUp, Statement statement) {
            this.times = times;
            this.warmUp = warmUp;
            this.statement = statement;
        }

        @Override
        public void evaluate() throws Throwable {
            for (int i = 0; i < times; i++) {
                statement.evaluate();
                if (warmUp > 0) {
                    Thread.currentThread().sleep(warmUp);
                }
            }
        }
    }

    @Override
    public Statement apply(Statement statement, Description description) {
        Statement result = statement;
        Repeat repeat = description.getAnnotation(Repeat.class);
        if (repeat != null) {
            int times = repeat.times();
            long warmUp = repeat.warmUp();
            result = new RepeatStatement(times, warmUp, statement);
        }
        return result;
    }

}
