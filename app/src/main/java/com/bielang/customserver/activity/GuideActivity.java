package com.bielang.customserver.activity;

import android.content.Intent;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bielang.customserver.R;
import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class GuideActivity extends AppCompatActivity {
    private int[] mImageIds = new int[]{R.drawable.pic_load1, R.drawable.pic_load2, R.drawable.pic_load3, R.drawable.pic_load4};
    private ArrayList<ImageView> mImageViewList;
    private LinearLayout llContainer;
    private ImageView ivRedPoint;
    private int mPaintDis;
    private TextView start_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_guide);
        ViewPager mViewPager = (ViewPager) findViewById(R.id.guide_page);
        llContainer = (LinearLayout) findViewById(R.id.ll_container);
        ivRedPoint = (ImageView) findViewById(R.id.iv_red);
        start_btn = (TextView) findViewById(R.id.start_btn);
        start_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(GuideActivity.this, LoginActivity.class);
                startActivity(intent);
                GuideActivity.this.finish();
            }
        });
        initData();
        GuideAdapter adapter = new GuideAdapter();
        mViewPager.setAdapter(adapter);

        //监听布局是否已经完成  布局的位置是否已经确定
        ivRedPoint.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                //避免重复回调        出于兼容性考虑，使用了过时的方法
                ivRedPoint.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                //布局完成了就获取第一个小灰点和第二个之间left的距离
                mPaintDis = llContainer.getChildAt(1).getLeft() - llContainer.getChildAt(0).getLeft();
            }
        });


        //ViewPager滑动Pager监听
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            //滑动过程中的回调
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                //当滑到第二个Pager的时候，positionOffset百分比会变成0，position会变成1，所以后面要加上position*mPaintDis
                int letfMargin = (int) (mPaintDis * positionOffset) + position * mPaintDis;
                //在父布局控件中设置他的leftMargin边距
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) ivRedPoint.getLayoutParams();
                params.leftMargin = letfMargin;
                ivRedPoint.setLayoutParams(params);
            }


            @Override
            public void onPageSelected(int position) {
                if (position == mImageViewList.size() - 1) {
                    start_btn.setVisibility(View.VISIBLE);
                }else
                    start_btn.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    private void initData() {
        mImageViewList = new ArrayList<>();
        for (int i = 0; i < mImageIds.length; i++) {
            //创建ImageView把mImgaeViewIds放进去
            ImageView view = new ImageView(this);
            Glide.with(this).load(mImageIds[i]).centerCrop().into(view);
            //添加到ImageView的集合中
            mImageViewList.add(view);


            //小圆点    一个小灰点是一个ImageView
            ImageView pointView = new ImageView(this);
            pointView.setImageResource(R.drawable.dot_unselected);
            //初始化布局参数，父控件是谁，就初始化谁的布局参数
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            if (i > 0) {
                //当添加的小圆点的个数超过一个的时候就设置当前小圆点的左边距为10dp;
                params.leftMargin = 20;
            }
            //设置小灰点的宽高包裹内容
            pointView.setLayoutParams(params);
            //将小灰点添加到LinearLayout中
            llContainer.addView(pointView);
        }
    }


    private class GuideAdapter extends PagerAdapter {

        //item的个数
        @Override
        public int getCount() {
            return mImageViewList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        //初始化item布局
        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            ImageView view = mImageViewList.get(position);
            container.addView(view);
            return view;
        }

        //销毁item
        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }

}
