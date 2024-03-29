package com.announce.common.notice.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.FragmentManager;

import com.google.gson.Gson;
import com.announce.common.notice.MyWebView;
import com.announce.common.notice.R;
import com.announce.common.notice.api.DataCallBack;
import com.announce.common.notice.api.RetrofitUtil;
import com.announce.common.notice.api.requestparameter.RequestParameter;
import com.announce.common.notice.entity.PopAnnounceData;

import okhttp3.ResponseBody;
import retrofit2.Call;

public class DialogManager {
    private PopAnnounceData popAnnounceData;

    private FragmentManager fragmentManager;
    private Context context;

    private RequestParameter parameter;

    private String titleBarColor;
    private String titleColor;
    private String backImageColor;
    private boolean isRelease = true;

    public DialogManager(FragmentManager fragmentManager, Context context) {
        this.fragmentManager = fragmentManager;
        this.context = context;
    }

    public void init(final Context context) {
//        requestData(context);

    }

    public void setPopAnnounceData(PopAnnounceData popAnnounceData) {
        this.popAnnounceData = popAnnounceData;
        if(popAnnounceData != null){
            showDialog(popAnnounceData);
        }

    }

    private void requestData() {
        Call<ResponseBody> call = RetrofitUtil.getInstance().getApiService().getPopAnnounceList(parameter);
        RetrofitUtil.getInstance().requestMode(call, new DataCallBack() {
            @Override
            public void success(String msg, String result) {
                Gson gson = new Gson();
                final PopAnnounceData popAnnounceData = gson.fromJson(result, PopAnnounceData.class);
                if (popAnnounceData == null || popAnnounceData.getAnnounce() == null) {
                    return;
                }
                showDialog(popAnnounceData);
            }

            @Override
            public void fail(String msg, int errorCode) {
                Log.e("error", msg);
            }
        });
    }

    private void showDialog(final PopAnnounceData popAnnounceData) {
        new SGDialog.Builder(fragmentManager)
                .setLayoutRes(R.layout.dialog_announce)
//                        .setScreenHeightAspect(context, 0.7f)
                .setScreenWidthAspect(context, 0.8f)
                .setCancelableOutside(false)
                .setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {

                        if (keyCode == KeyEvent.KEYCODE_BACK) {
                            if (popAnnounceData.getAnnounce().getCheckChoice().equals("1")) {//checkChoice查看选项1强制0不强制',
//                                        Toast.makeText(context, "强制按返回键无效", Toast.LENGTH_LONG).show();
                                return true;
                            }
                        }
                        return false;
                    }
                })
                .setOnBindViewListener(new OnBindViewListener() {
                    @Override
                    public void bindView(BindViewHolder viewHolder) {
                        LinearLayout view = viewHolder.getView(R.id.ll);
//                               TextView textViewContent = view.findViewById(R.id.tv_a_content);
                        TextView textViewTitle = view.findViewById(R.id.tv_title);
                        ImageView iv_close = viewHolder.bindView.findViewById(R.id.iv_close);
//                                textViewContent.setText(Html.fromHtml(htmlText));
                        if (popAnnounceData != null && popAnnounceData.getAnnounce() != null) {
                            textViewTitle.setText(popAnnounceData.getAnnounce().getAnnounceName());
                            if (popAnnounceData.getAnnounce().getCheckChoice().equals("1")) {//checkChoice查看选项1强制0不强制',
                                iv_close.setVisibility(View.GONE);
                            } else {
                                iv_close.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                })
                .addOnClickListener(R.id.iv_close, R.id.bt_check)
                .setOnViewClickListener(new OnViewClickListener() {
                    @Override
                    public void onViewClick(BindViewHolder viewHolder, View view, SGDialog tDialog) {
                        if (view.getId() == R.id.bt_check) {
                            //此处判断是否需要校验登录
                            if(popAnnounceData.isValidateLogin()) {
                                if(!TextUtils.isEmpty(popAnnounceData.getLoginToken())) {
                                    jumpToDetailPage(popAnnounceData);
                                }
                                else{
                                    Toast.makeText(view.getContext(), popAnnounceData.getNoLoginNote(), Toast.LENGTH_LONG).show();
                                }
                            }
                            else{
                                jumpToDetailPage(popAnnounceData);
                            }
                        }
                        tDialog.dismiss();
                    }
                })
                .create()
                .show();
    }

    /**
     * 查看详情
     * @param popAnnounceData
     */
    private void jumpToDetailPage(PopAnnounceData popAnnounceData) {
        if (popAnnounceData != null && !TextUtils.isEmpty(popAnnounceData.getDetailUrl())) {
            if (popAnnounceData.getAnnounce() != null && !TextUtils.isEmpty(popAnnounceData.getAnnounce().getAnnounceName())) {
                MyWebView.startActivity(context, popAnnounceData.getDetailUrl(), "消息中心",//popAnnounceData.getAnnounce().getAnnounceName()
                        titleColor, titleBarColor, backImageColor);
            } else {
                MyWebView.startActivity(context, popAnnounceData.getDetailUrl(), "消息中心", titleColor, titleBarColor, backImageColor);
            }

        }
    }

    public static class Builder {
        DialogManager dialogManager;

        public Builder(FragmentManager fragmentManager, Context context) {
            dialogManager = new DialogManager(fragmentManager, context);
        }

        public Builder setParameter(RequestParameter parameter) {
            dialogManager.parameter = parameter;
            return this;
        }

        public Builder setTitleBarBgColor(String color) {
            dialogManager.titleBarColor = color;
            return this;
        }

        public Builder setTitleColor(String color) {
            dialogManager.titleColor = color;
            return this;
        }

        public Builder setBackImageColor(String color) {
            dialogManager.backImageColor = color;
            return this;
        }

        public Builder setPopAnnounceData(PopAnnounceData entity){
            dialogManager.setPopAnnounceData(entity);
            return this;
        }

        public DialogManager create() {
//            dialogManager.requestData();
            return dialogManager;
        }

    }
}
