package com.mcxiaoke.next.task;

import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 一个用于执行异步任务的类，单例，支持检查Caller，支持按照Caller和Tag取消对应的任务
 * User: mcxiaoke
 * Date: 2013-7-1 2013-7-25 2014-03-04 2014-03-25
 * Date: 2014-05-14 2014-05-29 2015-06-15
 */
final class TaskQueueImpl extends TaskQueue {

    public static final String TAG = "TaskQueue";
    private final Object mLock = new Object();
    private ExecutorService mExecutor;
    private ExecutorService mSerialExecutor;
    private Map<String, List<String>> mGroups;
    private Map<String, ITaskRunnable> mRunnables;

    public TaskQueueImpl() {
        Log.v(TAG, "TaskQueue()");
        init();
        checkThread();
        checkExecutor();
    }

    private void init() {
        mGroups = new ConcurrentHashMap<String, List<String>>();
        mRunnables = new ConcurrentHashMap<String, ITaskRunnable>();
    }

    /********************************************************************
     *
     * PUBLIC METHODS
     *
     *******************************************************************/


    /**
     * 执行异步任务，回调时会检查Caller是否存在，如果不存在就不执行回调函数
     *
     * @param callable Callable对象，任务的实际操作
     * @param callback 回调接口
     * @param caller   调用方，一般为Fragment或Activity
     * @param serial   是否按顺序执行任务
     * @param <Result> 类型参数，异步任务执行结果
     * @return 返回内部生成的此次任务的TAG
     */
    @Override
    public <Result> String execute(final Callable<Result> callable,
                                   final TaskCallback<Result> callback,
                                   final Object caller, final boolean serial) {
        checkArguments(callable, caller);
        return execute(TaskBuilder.create(callable).with(caller).callback(callback)
                .serial(serial).on(this).create());
    }

    @Override
    public <Result> String add(final Callable<Result> callable,
                               final TaskCallback<Result> callback,
                               final Object caller) {
        return execute(callable, callback, caller, false);
    }

    /**
     * @param callable Callable
     * @param caller   Caller
     * @param <Result> Result
     * @return Tag
     */
    @Override
    public <Result> String add(final Callable<Result> callable, final Object caller) {
        return add(callable, null, caller);
    }

    @Override
    public <Result> String addSerially(final Callable<Result> callable,
                                       final TaskCallback<Result> callback, final Object caller) {
        return execute(callable, callback, caller, true);
    }

    /**
     * @param callable Callable
     * @param caller   Caller
     * @param <Result> Result
     * @return Tag
     */
    @Override
    public <Result> String addSerially(final Callable<Result> callable, final Object caller) {
        return addSerially(callable, null, caller);
    }

    /**
     * 取消NAME对应的任务
     *
     * @param name 任务NAME
     * @return 任务是否存在
     */
    @Override
    public boolean cancel(String name) {
        return cancelByName(name);
    }

    /**
     * 取消由该调用方发起的所有任务
     * 建议在Fragment或Activity的onDestroy中调用
     *
     * @param caller 任务调用方
     * @return 返回取消的数目
     */
    @Override
    public int cancelAll(Object caller) {
        return cancelByCaller(caller);
    }

    /**
     * 设置自定义的ExecutorService
     *
     * @param executor ExecutorService
     */
    @Override
    public void setExecutor(final ExecutorService executor) {
        if (executor == null) {
            throw new NullPointerException("executor must not be null.");
        }
        mExecutor = executor;
    }

    /**
     * 便利任务列表，取消所有任务
     */
    @Override
    public void cancelAll() {
        if (Config.DEBUG) {
            Log.v(TAG, "cancelAll()");
        }
        cancelAllInQueue();
    }

    /**
     * 获取当前实例的详细信息
     *
     * @return dump output
     */
    @Override
    public String dump() {
        final StringBuilder builder = new StringBuilder();
        if (mExecutor instanceof ThreadPoolExecutor) {
            // thread pool info
            final ThreadPoolExecutor executor = (ThreadPoolExecutor) mExecutor;
            final int corePoolSize = executor.getCorePoolSize();
            final int poolSize = executor.getPoolSize();
            final int activeCount = executor.getActiveCount();
            final long taskCount = executor.getTaskCount();
            final long completedCount = executor.getCompletedTaskCount();
            final boolean isShutdown = executor.isShutdown();
            final boolean isTerminated = executor.isTerminated();
            builder.append(TAG).append("[ ");
            builder.append("ThreadPool:{")
                    .append(" coreSize:").append(corePoolSize).append(";")
                    .append(" poolSize:").append(poolSize).append(";")
                    .append(" isShutdown:").append(isShutdown).append(";")
                    .append(" isTerminated:").append(isTerminated).append(";")
                    .append(" activeCount:").append(activeCount).append(";")
                    .append(" taskCount:").append(taskCount).append(";")
                    .append(" completedCount:").append(completedCount).append(";")
                    .append("}\n");
        } else {
            // thread pool info
            final ExecutorService executor = mExecutor;
            final boolean isShutdown = executor.isShutdown();
            final boolean isTerminated = executor.isTerminated();
            builder.append(TAG).append("[ ");
            builder.append("ThreadPool:{")
                    .append(" isShutdown:").append(isShutdown).append(";")
                    .append(" isTerminated:").append(isTerminated).append(";")
                    .append("}\n");
        }
        // group map
        final Map<String, List<String>> callerMap = mGroups;
        builder.append("Groups:{");
        for (Map.Entry<String, List<String>> entry : callerMap.entrySet()) {
            builder.append(" group:").append(entry.getKey())
                    .append(", tags:").append(Utils.toString(entry.getValue())).append(";");
        }
        builder.append("}\n");
        builder.append("]");

        // task map
        Map<String, ITaskRunnable> taskMap = mRunnables;
        builder.append("Tasks:{");
        for (Map.Entry<String, ITaskRunnable> entry : taskMap.entrySet()) {
            builder.append(" tag:").append(entry.getKey())
                    .append(", runnable:").append(entry.getValue()).append(";");
        }
        builder.append("}\n");

        final String info = builder.toString();
        Log.v(TAG, info);
        return info;
    }

    /********************************************************************
     *
     * PRIVATE METHODS
     *
     *******************************************************************/

    /**
     * 执行异步任务
     *
     * @param task 任务对象
     * @return 返回内部生成的此次任务的TAG
     */
    @Override
    String execute(final Task task) {
        if (Config.DEBUG) {
            Log.v(TAG, "execute() task=" + task);
        }
        assert task != null;
        final ITaskRunnable runnable = TaskFactory.createRunnable(task);
        final boolean serial = task.isSerial();
        final String group = task.getGroup();
        final String name = task.getName();
        addToRunnableMap(name, runnable);
        addToGroupMap(name, group);
        smartSubmit(runnable, serial);
        return name;
    }

    @Override
    boolean cancel(final TaskFuture task) {
        return cancelByName(task.getName());
    }

    private void addToRunnableMap(final String tag, final ITaskRunnable runnable) {
        synchronized (mLock) {
            mRunnables.put(tag, runnable);
        }
    }

    private void addToGroupMap(final String name, final String group) {
        List<String> tags = mGroups.get(group);
        if (tags == null) {
            synchronized (mLock) {
                tags = new ArrayList<String>();
                mGroups.put(group, tags);
            }
        }
        synchronized (mLock) {
            tags.add(name);
        }
    }

    /**
     * 取消所有的Runnable对应的任务
     */
    private void cancelAllInQueue() {
        if (Config.DEBUG) {
            Log.v(TAG, "cancelAllInQueue()");
        }
        long start = SystemClock.elapsedRealtime();
        final Collection<ITaskRunnable> tasks = mRunnables.values();
        for (ITaskRunnable task : tasks) {
            if (task != null) {
                task.cancel();
            }
        }
        synchronized (mLock) {
            mRunnables.clear();
            mGroups.clear();
        }
        if (Config.DEBUG) {
            final long time = SystemClock.elapsedRealtime() - start;
            Log.v(TAG, "cancelAllInQueue() using " + time + "ms");
        }
    }

    int cancelByCaller(final Object caller) {
        if (Config.DEBUG) {
            Log.v(TAG, "cancelByCaller() caller=" + caller);
        }
        return cancelByGroup(TaskTag.getGroup(caller));
    }

    int cancelByGroup(final String group) {
        if (Config.DEBUG) {
            Log.v(TAG, "cancelByGroup() group=" + group);
        }
        long start = SystemClock.elapsedRealtime();
        int count = 0;
        final List<String> names;
        synchronized (mLock) {
            names = mGroups.remove(group);
        }
        if (names == null || names.isEmpty()) {
            return count;
        }
        for (String name : names) {
            cancelByName(name);
            ++count;
        }
        if (Config.DEBUG) {
            final long time = SystemClock.elapsedRealtime() - start;
            Log.v(TAG, "cancelByGroup() count=" + count + " using " + time + "ms");
        }
        return count;
    }

    boolean cancelByName(final String name) {
        if (Config.DEBUG) {
            Log.v(TAG, "cancelByName() name=" + name);
        }
        boolean result = false;
        final ITaskRunnable runnable;
        synchronized (mLock) {
            runnable = mRunnables.remove(name);
        }
        if (runnable != null) {
            result = runnable.cancel();
        }
        return result;
    }


    @Override
    void remove(final TaskFuture task) {
        if (Config.DEBUG) {
            Log.v(TAG, "remove() " + task.getName());
        }
        synchronized (mLock) {
            mRunnables.remove(task.getName());
        }
        List<String> tags = mGroups.get(task.getGroup());
        if (tags != null) {
            synchronized (mLock) {
                tags.remove(task.getName());
            }
        }
    }

    /**
     * 将任务添加到线程池执行
     *
     * @param runnable 任务Runnable
     */
    private void smartSubmit(final ITaskRunnable runnable, final boolean serial) {
        checkExecutor();
        final Future<?> future;
        if (serial) {
            future = mSerialExecutor.submit(runnable);
        } else {
            future = mExecutor.submit(runnable);
        }
        runnable.setFuture(future);
    }

    /**
     * 检查并初始化ExecutorService
     */
    private void checkExecutor() {
        if (mExecutor == null || mExecutor.isShutdown()) {
            mExecutor = Utils.newCachedThreadPool("task-default");
        }
        if (mSerialExecutor == null || mSerialExecutor.isShutdown()) {
            mSerialExecutor = Utils.newSingleThreadExecutor("task-serial");
        }
    }

    /**
     * 关闭Executor
     */
    private void destroyExecutor() {
        if (mExecutor != null) {
            mExecutor.shutdown();
            mExecutor = null;
        }
        if (mSerialExecutor != null) {
            mSerialExecutor.shutdown();
            mSerialExecutor = null;
        }
    }

    /********************************************************************
     * STATIC METHODS
     *******************************************************************/

    private static void checkThread() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            throw new IllegalStateException("TaskQueue instance must be created on main thread");
        }
    }

    /**
     * 检查参数非空
     *
     * @param args 参数列表
     */
    private static void checkArguments(final Object... args) {
        for (Object o : args) {
            if (o == null) {
                throw new NullPointerException("argument can not be null " + Arrays.toString(args));
            }
        }
    }

    private static void logExecutor(final String name, final ThreadPoolExecutor executor) {
        final int corePoolSize = executor.getCorePoolSize();
        final int poolSize = executor.getPoolSize();
        final int activeCount = executor.getActiveCount();
        final long taskCount = executor.getTaskCount();
        final long completedCount = executor.getCompletedTaskCount();
        final boolean isShutdown = executor.isShutdown();
        final boolean isTerminated = executor.isTerminated();
        Log.v(TAG, name + " CorePoolSize:" + corePoolSize + " PoolSize:" + poolSize);
        Log.v(TAG, name + " isShutdown:" + isShutdown + " isTerminated:" + isTerminated);
        Log.v(TAG, name + " activeCount:" + activeCount + " taskCount:" + taskCount
                + " completedCount:" + completedCount);
    }

}
