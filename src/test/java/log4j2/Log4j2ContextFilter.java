package log4j2;

import com.alibaba.mtc.MtContextRunnable;
import com.alibaba.mtc.MtContextThreadLocal;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @author ding.lid
 */
public class Log4j2ContextFilter {
    static Logger logger = LogManager.getLogger(Log4j2ContextFilter.class);

    // log4j2 context
    static MtContextThreadLocal<Map<String, String>> mtc = new MtContextThreadLocal<Map<String, String>>() {
        @Override
        protected void beforeExecute() {
            final Map<String, String> log4j2Context = get();
            for (Map.Entry<String, String> entry : log4j2Context.entrySet()) {
                ThreadContext.put(entry.getKey(), entry.getValue());
            }
        }

        @Override
        protected void afterExecute() {
            ThreadContext.clearAll();
        }

        @Override
        protected Map<String, String> initialValue() {
            return new HashMap<String, String>();
        }
    };

    public static void main(String[] args) throws Exception {
        // Init Log Context, set MTC
        // More KV if needed
        final String TRACE_ID = "trace-id";
        final String TRACE_ID_VALUE = "XXX-YYY-ZZZ";
        ThreadContext.put(TRACE_ID, TRACE_ID_VALUE);
        mtc.get().put(TRACE_ID, TRACE_ID_VALUE);

        // Log in Main Thread
        logger.info("Log in main!");

        // Run task in thread pool
        final ExecutorService executorService = Executors.newFixedThreadPool(1);
        final Runnable task = new Runnable() {
            @Override
            public void run() {
                // Log in thread pool
                logger.info("Log in Runnable!");
            }
        };
        final Future<?> submit = executorService.submit(MtContextRunnable.get(task));
        submit.get();

        executorService.shutdown();
    }
}
