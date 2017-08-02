package com.bielang.customserver.emotion;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.bielang.customserver.R;

import java.util.ArrayList;
import java.util.List;

import static com.bielang.customserver.DataName.EMOTION_CLASSIC_TYPE;

public class EmotionMainFragment extends BaseFragment {

    //是否绑定当前Bar的编辑框的flag
    public static final String BIND_TO_EDITTEXT="bind_to_edittext";
    //是否隐藏bar上的编辑框和发生按钮
    public static final String HIDE_BAR_EDITTEXT_AND_BTN="hide bar's editText and btn";

    //当前被选中底部tab
    private static final String CURRENT_POSITION_FLAG="CURRENT_POSITION_FLAG";
    private int CurrentPosition=0;
    //底部水平tab
    private RecyclerView recyclerview_horizontal;
    private HorizontalRecyclerviewAdapter horizontalRecyclerviewAdapter;
    //表情面板
    private EmotionKeyboard mEmotionKeyboard;
    private EmotionKeyboard mMoreActionKeyboard;

    private EditText bar_edit_text;
    private TextView bar_btn_send;

    //需要绑定的内容view
    private View contentView;

    //不可横向滚动的ViewPager
    private NoHorizontalScrollerViewPager viewPager;

    //是否绑定当前Bar的编辑框,默认true,即绑定。
    //false,则表示绑定contentView,此时外部提供的contentView必定也是EditText
    private boolean isBindToBarEditText=true;

    //是否隐藏bar上的编辑框和发生按钮,默认不隐藏
    private boolean isHidenBarEditTextAndBtn=false;

    List<Fragment> fragments=new ArrayList<>();

    private LinearLayout order_edit;
    private LinearLayout ad;
    private LinearLayout quick_reply;
    private ListView quick_reply_list;
    private View rootView;

    /**
     * 创建与Fragment对象关联的View视图时调用
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_main_emotion, container, false);
        isHidenBarEditTextAndBtn= args.getBoolean(EmotionMainFragment.HIDE_BAR_EDITTEXT_AND_BTN);
        //获取判断绑定对象的参数
        isBindToBarEditText=args.getBoolean(EmotionMainFragment.BIND_TO_EDITTEXT);
        initView(rootView);
        mEmotionKeyboard = EmotionKeyboard.with(getActivity())
                .setEmotionView(rootView.findViewById(R.id.ll_emotion_layout))//绑定表情面板
                .bindToContent(contentView)//绑定内容view
                .bindToEditText(!isBindToBarEditText ? ((EditText) contentView) : ((EditText) rootView.findViewById(R.id.InputBox)))//判断绑定那种EditView
                .build();
        mMoreActionKeyboard=EmotionKeyboard.with(getActivity())
                .setEmotionView(rootView.findViewById(R.id.ll_more_layout))
                .bindToContent(contentView)
                .bindToEditText(!isBindToBarEditText ? ((EditText) contentView) : ((EditText) rootView.findViewById(R.id.InputBox)))//判断绑定那种EditView
                .build();

        order_edit=mMoreActionKeyboard.getmContentView().findViewById(R.id.order_edit);
        ad=mMoreActionKeyboard.getmContentView().findViewById(R.id.ad);
        quick_reply=mMoreActionKeyboard.getmContentView().findViewById(R.id.quick_reply);
        quick_reply_list=mMoreActionKeyboard.getmContentView().findViewById(R.id.quick_reply_list);
        initListener();
        initDatas();
        //创建全局监听
        GlobalOnItemClickManagerUtils globalOnItemClickManager= GlobalOnItemClickManagerUtils.getInstance(getActivity());

        if(isBindToBarEditText){
            //绑定当前Bar的编辑框
            globalOnItemClickManager.attachToEditText(bar_edit_text);

        }else{
            // false,则表示绑定contentView,此时外部提供的contentView必定也是EditText
            globalOnItemClickManager.attachToEditText((EditText) contentView);
            mEmotionKeyboard.bindToEditText((EditText)contentView);
        }
        return rootView;
    }

    /**
     * 绑定内容view
     */
    public void bindToContentView(View contentView){
        this.contentView=contentView;
    }

    /**
     * 初始化view控件
     */
    protected void initView(View rootView){
        viewPager= rootView.findViewById(R.id.vp_emotionview_layout);
        recyclerview_horizontal=  rootView.findViewById(R.id.recyclerview_horizontal);
        bar_edit_text=  rootView.findViewById(R.id.InputBox);
        bar_btn_send=  rootView.findViewById(R.id.BtnSend);
        if(isHidenBarEditTextAndBtn){//隐藏
            bar_edit_text.setVisibility(View.GONE);
            bar_btn_send.setVisibility(View.GONE);
        }else{
            bar_edit_text.setVisibility(View.VISIBLE);
            bar_btn_send.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 初始化监听器
     */
    protected void initListener(){
        rootView.findViewById(R.id.emotion_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mMoreActionKeyboard.isEmotionLayoutShown())
                    mMoreActionKeyboard.hideEmotionLayout(false);
                if (mEmotionKeyboard.isEmotionLayoutShown()){
                    mEmotionKeyboard.lockContentHeight();
                    mEmotionKeyboard.hideEmotionLayout(true);
                    mEmotionKeyboard.unlockContentHeightDelayed();
                }else{
                    if (mEmotionKeyboard.isSoftInputShown()){
                        mEmotionKeyboard.lockContentHeight();
                        mEmotionKeyboard.showEmotionLayout();
                        mEmotionKeyboard.unlockContentHeightDelayed();
                    }else
                        mEmotionKeyboard.showEmotionLayout();
                }
            }
        });
        rootView.findViewById(R.id.work_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (quick_reply_list.getVisibility() == View.VISIBLE) {
                    quick_reply_list.setVisibility(View.GONE);
                    quick_reply.setVisibility(View.VISIBLE);
                    ad.setVisibility(View.VISIBLE);
                    order_edit.setVisibility(View.VISIBLE);
                }
                if (mEmotionKeyboard.isEmotionLayoutShown())
                    mEmotionKeyboard.hideEmotionLayout(false);
                if (mMoreActionKeyboard.isEmotionLayoutShown()){
                    mMoreActionKeyboard.lockContentHeight();
                    mMoreActionKeyboard.hideEmotionLayout(true);
                    mMoreActionKeyboard.unlockContentHeightDelayed();
                }else{
                    if (mMoreActionKeyboard.isSoftInputShown()){
                        mMoreActionKeyboard.lockContentHeight();
                        mMoreActionKeyboard.showEmotionLayout();
                        mMoreActionKeyboard.unlockContentHeightDelayed();
                    }else
                        mMoreActionKeyboard.showEmotionLayout();
                }
            }
        });
        rootView.findViewById(R.id.InputBox).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                final EmotionKeyboard keyboard;
                if (mEmotionKeyboard.isEmotionLayoutShown())
                    keyboard=mEmotionKeyboard;
                else
                    keyboard=mMoreActionKeyboard;
                if (event.getAction() == MotionEvent.ACTION_UP && keyboard.isEmotionLayoutShown()) {
                    keyboard.lockContentHeight();
                    keyboard.hideEmotionLayout(true);
                    rootView.findViewById(R.id.InputBox).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            keyboard.unlockContentHeightDelayed();
                        }
                    }, 200L);
                }
                return false;
            }
        });
    }

    /**
     * 数据操作,这里是测试数据，请自行更换数据
     */
    @SuppressWarnings("deprecation")
    protected void initDatas(){
        replaceFragment();
        List<ImageModel> list = new ArrayList<>();
        for (int i=0 ; i<fragments.size(); i++){
//            if(i==0){
                ImageModel model1=new ImageModel();
                model1.icon= getResources().getDrawable(R.drawable.ic_emotion);
                model1.flag="经典笑脸";
                model1.isSelected=true;
                list.add(model1);
//            }else {
//                ImageModel model = new ImageModel();
//                model.icon = getResources().getDrawable(R.drawable.ic_plus);
//                model.flag = "其他笑脸" + i;
//                model.isSelected = false;
//                list.add(model);
//            }
        }

        //记录底部默认选中第一个
        CurrentPosition=0;
        SharedPreferencedUtils.setInteger(getActivity(), CURRENT_POSITION_FLAG, CurrentPosition);

        //底部tab
        horizontalRecyclerviewAdapter = new HorizontalRecyclerviewAdapter(getActivity(),list);
        recyclerview_horizontal.setHasFixedSize(true);//使RecyclerView保持固定的大小,这样会提高RecyclerView的性能
        recyclerview_horizontal.setAdapter(horizontalRecyclerviewAdapter);
        recyclerview_horizontal.setLayoutManager(new GridLayoutManager(getActivity(), 1, GridLayoutManager.HORIZONTAL, false));
        //初始化recyclerview_horizontal监听器
        horizontalRecyclerviewAdapter.setOnClickItemListener(new HorizontalRecyclerviewAdapter.OnClickItemListener() {
            @Override
            public void onItemClick(View view, int position, List<ImageModel> datas) {
                //获取先前被点击tab
                int oldPosition = SharedPreferencedUtils.getInteger(getActivity(), CURRENT_POSITION_FLAG, 0);
                //修改背景颜色的标记
                datas.get(oldPosition).isSelected = false;
                //记录当前被选中tab下标
                CurrentPosition = position;
                datas.get(CurrentPosition).isSelected = true;
                SharedPreferencedUtils.setInteger(getActivity(), CURRENT_POSITION_FLAG, CurrentPosition);
                //通知更新，这里我们选择性更新就行了
                horizontalRecyclerviewAdapter.notifyItemChanged(oldPosition);
                horizontalRecyclerviewAdapter.notifyItemChanged(CurrentPosition);
                //viewpager界面切换
                viewPager.setCurrentItem(position,false);
            }

            @Override
            public void onItemLongClick(View view, int position, List<ImageModel> datas) {
            }
        });



    }

    private void replaceFragment(){
        //创建fragment的工厂类
        FragmentFactory factory=FragmentFactory.getSingleFactoryInstance();
        //创建修改实例
        EmotiomComplateFragment f1= (EmotiomComplateFragment) factory.getFragment(EMOTION_CLASSIC_TYPE);
        fragments.add(f1);
//        Bundle b=null;
        //添加其他表情页
//        for (int i=0;i<1;i++){
//            b=new Bundle();
//            b.putString("Interge","Fragment-"+i);
//            Fragment1 fg= Fragment1.newInstance(Fragment1.class,b);
//            fragments.add(fg);
//        }

        NoHorizontalScrollerVPAdapter adapter =new NoHorizontalScrollerVPAdapter(getActivity().getSupportFragmentManager(),fragments);
        viewPager.setAdapter(adapter);
    }


    /**
     * 是否拦截返回键操作，如果此时表情布局未隐藏，先隐藏表情布局
     * @return true则隐藏表情布局，拦截返回键操作
     *         false 则不拦截返回键操作
     */
    public boolean isInterceptBackPress(){
        return mEmotionKeyboard.interceptBackPress() || mMoreActionKeyboard.interceptBackPress();
    }

    public LinearLayout getView(int witch){
        if (witch==0)
            return order_edit;
        else if (witch==1)
            return ad;
        return quick_reply;
    }
    public ListView getList(){
        return quick_reply_list;
    }
}