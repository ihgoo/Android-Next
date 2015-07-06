package com.mcxiaoke.next.recycler;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import com.mcxiaoke.next.utils.LogUtils;

/**
 * User: mcxiaoke
 * Date: 15/5/27
 * Time: 17:14
 */
public class EndlessRecyclerView extends RecyclerView {
    public static final String TAG = EndlessRecyclerView.class.getSimpleName();
    public static final int DEFAULT_THRESHOLD = 3;

    public interface OnLoadMoreListener {
        void onLoadMore(final EndlessRecyclerView view);
    }

    public static final int STATE_HIDE = 0;
    public static final int STATE_PROGRESS = 1;
    public static final int STATE_TEXT = 2;

    public static final int MODE_AUTO = 0;
    public static final int MODE_MANUAL = 1;
    public static final int MODE_NONE = 2;

    public static class ViewState {
        private int mode;
        private int state;
        private int threshold;
        private int index;
        private CharSequence text;

        public ViewState() {
            reset();
        }

        public ViewState(final ViewState state) {
            this.mode = state.mode;
            this.state = state.state;
            this.threshold = state.threshold;
            this.index = state.index;
        }

        public ViewState copy() {
            return new ViewState(this);
        }

        private void reset() {
            mode = MODE_AUTO;
            state = STATE_HIDE;
            threshold = DEFAULT_THRESHOLD;
            index = 0;
        }

        public int getState() {
            return state;
        }

        public ViewState setState(final int state) {
            this.state = state;
            return this;
        }

        public int getMode() {
            return mode;
        }

        public ViewState setMode(final int mode) {
            this.mode = mode;
            return this;
        }

        public int getThreshold() {
            return threshold;
        }

        public ViewState setThreshold(final int threshold) {
            this.threshold = threshold;
            return this;
        }

        public int getIndex() {
            return index;
        }

        public ViewState incIndex() {
            this.index++;
            return this;
        }

        public ViewState setIndex(final int index) {
            this.index = index;
            return this;
        }

        public CharSequence getText() {
            return text;
        }

        public ViewState setText(final CharSequence text) {
            this.text = text;
            return this;
        }

        @Override
        public String toString() {
            return "ViewState{" +
                    "display=" + state +
                    ", mode=" + mode +
                    ", threshold=" + threshold +
                    '}';
        }
    }

    private EndlessRecyclerView mRecyclerView = this;
    private ViewState mViewState = new ViewState();
    // The total number of items in the data set after the last load
    private int mPreviousTotal = 0;
    private EndlessRecyclerAdapter mAdapter;
    private OnLoadMoreListener mLoadMoreListener;
    private volatile boolean mLoading;

    private RecyclerView.OnScrollListener mEndlessScrollListener = new OnScrollListener() {
        @Override
        public void onScrolled(final RecyclerView recyclerView, final int dx, final int dy) {
            super.onScrolled(recyclerView, dx, dy);
        }

        @Override
        public void onScrollStateChanged(final RecyclerView recyclerView, final int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            LogUtils.v(TAG, "onScrolled() state=" + mViewState);
            if (mViewState.mode != MODE_AUTO) {
                return;
            }
            if (mLoading) {
                return;
            }
            final RecyclerViewHelper recyclerViewHelper = new RecyclerViewHelper(recyclerView);
            final int threshold = mViewState.getThreshold();
            int visibleItemCount = recyclerView.getChildCount();
            int totalItemCount = recyclerViewHelper.getItemCount();
            int firstVisibleItem = recyclerViewHelper.findFirstVisibleItemPosition();
            if ((totalItemCount - visibleItemCount)
                    <= (firstVisibleItem + threshold)) {
                mViewState.incIndex();

                LogUtils.v(TAG, "onScrolled() onLoadMore() pageIndex =" + mViewState.getIndex());
                setLoadingState(true);
                if (mLoadMoreListener != null) {
                    mLoadMoreListener.onLoadMore(mRecyclerView);
                }
            }
        }
    };

    public EndlessRecyclerView(final Context context) {
        super(context);
        init(context);
    }

    public EndlessRecyclerView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public EndlessRecyclerView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(final Context context) {
        super.setLayoutManager(new LinearLayoutManager(context));
        addOnScrollListener(mEndlessScrollListener);
    }

    @Override
    public void setLayoutManager(final LayoutManager layout) {
        LogUtils.w(TAG, "using LinearLayoutManager by default, ignore custom layout manager");
//        super.setLayoutManager(layout);
    }

    @Override
    public void setAdapter(final Adapter adapter) {
        if (adapter == null) {
            mAdapter = null;
            super.setAdapter(null);
        } else {
            mAdapter = new EndlessRecyclerAdapter(adapter, mViewState);
            super.setAdapter(mAdapter);
        }
    }

    private void updateState() {
        if (mAdapter != null) {
            mAdapter.updateState();
        }
    }

    private EndlessRecyclerView setMode(final int mode) {
        if (mode < MODE_AUTO || mode > MODE_NONE) {
            throw new IllegalArgumentException("mode must between " + MODE_AUTO
                    + " and " + MODE_NONE);
        }
        LogUtils.v(TAG, "setMode() mode=" + mode);
        mViewState.setMode(mode);
        updateState();
        return this;
    }

    private void setLoadingState(final boolean loading) {
        LogUtils.v(TAG, "setLoadingState() loading=" + loading);
        if (loading) {
            showProgress();
        } else {
            showEmpty();
        }
    }

    public EndlessRecyclerView enable(boolean enable) {
        setMode(enable ? MODE_AUTO : MODE_NONE);
        return this;
    }

    public void showProgress() {
        if (mViewState.getMode() != MODE_NONE) {
            mLoading = true;
            mViewState.setState(STATE_PROGRESS);
            updateState();
        } else {
            LogUtils.w(TAG, "showProgress() ignore, endless mode is disabled");
        }
    }

    public void showEmpty() {
        mLoading = false;
        mViewState.setState(STATE_HIDE);
        updateState();
    }

    public void showText(final CharSequence text) {
        LogUtils.v(TAG, "showText() text=" + text);
        mLoading = false;
        mViewState.setText(text).setState(STATE_TEXT);
        updateState();
    }

    public void onComplete() {
        LogUtils.v(TAG, "onComplete()");
        setLoadingState(false);
    }

    public void setLoading(boolean loading) {
        LogUtils.v(TAG, "setLoading() loading=" + loading);
        setLoadingState(loading);
    }

    public EndlessRecyclerView setOnLoadMoreListener(final OnLoadMoreListener listener) {
        mLoadMoreListener = listener;
        return this;
    }

    public EndlessRecyclerView setLoadMoreThreshold(final int threshold) {
        LogUtils.v(TAG, "setLoadMoreThreshold() threshold=" + threshold);
        mViewState.setThreshold(threshold);
        return this;
    }

    public boolean isLoadingMore() {
        return mLoading;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeOnScrollListener(mEndlessScrollListener);
    }
}
