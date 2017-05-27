package net.oschina.app.improve.widget;

import android.content.Context;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.bumptech.glide.RequestManager;

import net.oschina.app.R;
import net.oschina.app.bean.Banner;
import net.oschina.app.improve.widget.indicator.CirclePagerIndicator;
import net.oschina.app.widget.SmoothScroller;
import net.oschina.app.widget.SuperRefreshLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by huanghaibin
 * on 16-6-2.
 * <p>
 * Changed qiujuer
 * on 08-18
 */
public class ViewEventHeader extends RelativeLayout implements ViewPager.OnPageChangeListener, Runnable {
    private ViewPager vp_event;
    private List<ViewEventBanner> banners = new ArrayList<>();
    private EventPagerAdapter adapter;
    private SuperRefreshLayout refreshLayout;
    private CirclePagerIndicator indicator;
    private int mCurrentItem = 0;
    private Handler mHandler;
    private boolean isMoving = false;
    private boolean isScroll = false;


    public ViewEventHeader(Context context) {
        super(context);
        init(context);
    }

    public ViewEventHeader(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public void setRefreshLayout(SuperRefreshLayout refreshLayout) {
        this.refreshLayout = refreshLayout;
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.item_list_event_header, this, true);
        vp_event = (ViewPager) findViewById(R.id.vp_event);
        indicator = (CirclePagerIndicator) findViewById(R.id.indicator);
        adapter = new EventPagerAdapter();
        vp_event.setAdapter(adapter);
        indicator.bindViewPager(vp_event);
        new SmoothScroller(getContext()).bingViewPager(vp_event);
        vp_event.addOnPageChangeListener(this);
        vp_event.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_UP:
                        isMoving = false;
                        refreshLayout.setEnabled(true);
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        isMoving = false;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        refreshLayout.setEnabled(false);
                        isMoving = true;
                        break;
                }
                return false;
            }
        });
    }

    public void initData(RequestManager manager, List<Banner> banners) {
        this.banners.clear();
        for (Banner banner : banners) {
            ViewEventBanner eventBanner = new ViewEventBanner(getContext());
            eventBanner.initData(manager, banner);
            this.banners.add(eventBanner);
        }
        adapter.notifyDataSetChanged();
        indicator.notifyDataSetChanged();
        requestNext();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        isMoving = mCurrentItem != position;
    }

    @Override
    public void onPageSelected(int position) {
        isMoving = false;
        mCurrentItem = position;
        refreshLayout.setEnabled(true);
        isScroll = false;
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        isMoving = state != ViewPager.SCROLL_STATE_IDLE;
        isScroll = state != ViewPager.SCROLL_STATE_IDLE;
        refreshLayout.setEnabled(state == ViewPager.SCROLL_STATE_IDLE);
    }


    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mHandler = new Handler();
        // show first
        run();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mHandler.removeCallbacksAndMessages(null);
        mHandler = null;
    }

    private void requestNext() {
        Handler handler = this.mHandler;
        if (handler != null && banners.size() > 1) {
            // do next
            handler.postDelayed(this, 5000);
        }
    }

    @Override
    public void run() {
        if (banners.size() == 0)
            return;
        if (!isMoving && !isScroll) {
            mCurrentItem = (mCurrentItem + 1) % banners.size();
            vp_event.setCurrentItem(mCurrentItem);
        }
        requestNext();
    }

    private class EventPagerAdapter extends PagerAdapter {
        @Override
        public int getCount() {
            return banners.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(banners.get(position));
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            container.addView(banners.get(position));
            return banners.get(position);
        }
    }
}
