package com.shu.studentmanager.adpater;

import static android.content.ContentValues.TAG;

import static com.shu.studentmanager.activity.LoginActivity.localUrl;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.shu.studentmanager.R;
import com.shu.studentmanager.StudentManagerApplication;
import com.shu.studentmanager.activity.MainActivity;
import com.shu.studentmanager.constant.RequestConstant;
import com.shu.studentmanager.entity.Course;
import com.shu.studentmanager.entity.CourseTeacher;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CourseManageAdapter extends RecyclerView.Adapter<CourseManageAdapter.ViewHolder> {
    Context context;
    ArrayList<CourseTeacher> courseManageList;
    ArrayList<Course> courseArrayList;

    public CourseManageAdapter(Context context) {
        this.courseManageList = new ArrayList<CourseTeacher>();
        this.context = context;
    }

    public CourseManageAdapter(Context context, ArrayList<CourseTeacher> courseManageList) {
        this.context = context;
        this.courseManageList = courseManageList;
    }

    public CourseManageAdapter() {
        this.courseManageList = new ArrayList<CourseTeacher>();
    }

    //创建viewHolder
    @NonNull
    @Override
    public CourseManageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_course_manage, parent, false);
        return new CourseManageAdapter.ViewHolder(view);
    }

    //绑定viewHolder
    @Override
    public void onBindViewHolder(@NonNull CourseManageAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        if (courseManageList != null && courseManageList.size() > 0) {
            CourseTeacher courseEntity = courseManageList.get(position);
            holder.course_id_cid.setText(courseEntity.getCid());
            holder.course_name_cname.setText(courseEntity.getCname());
            holder.course_credit_ccredit.setText(courseEntity.getCcredit());
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                //                仅仅让编译通过
                @RequiresApi(api = Build.VERSION_CODES.P)
                @Override
                public void onClick(View view) {
                    showAlertDialogMode(position, holder.course_name_cname);
                }
            });
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    new MaterialAlertDialogBuilder(context)
                            .setTitle("确认")
                            .setMessage("确定删除该课程？")
                            .setNeutralButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            })
                            .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Log.d(TAG, "onClick: " + which + " " + position);
                                    enSureDelete(courseManageList.get(position));
                                }
                            }).show();
                    return false;
                }
            });

        } else {
            return;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    private void showAlertDialogMode(int position, TextView student_password) {
//        初始化输入框
        final EditText input = new EditText(context);
//        输入类型
        input.setInputType(InputType.TYPE_CLASS_TEXT);
//        输入框高度
        input.setLineHeight(12);
//        设置布局
        FrameLayout container = new FrameLayout(context);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.leftMargin = context.getResources().getDimensionPixelSize(R.dimen.fab_margin);
        params.rightMargin = context.getResources().getDimensionPixelSize(R.dimen.fab_margin);
        input.setLayoutParams(params);
        container.addView(input);
//        弹出框
        new MaterialAlertDialogBuilder(context)
                .setTitle("更新课程信息")
                .setMessage("输入：")
                .setView(container)
                .setNeutralButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "onClick: " + which + " " + position + " " + input.getText().toString());
                        try {
                            Log.i("te", "onClick: " + position);
                            changeCourse(input.getText().toString(), courseManageList.get(position));

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Log.i("te", "course: " + input.getText().toString());
                        student_password.setText(input.getText().toString());
                    }
                }).show();
    }

    //修改课程（名）信息
    private void changeCourse(String s, CourseTeacher courseTeacher) throws JSONException {
        MediaType JSON = MediaType.parse("application/json;charset=utf-8");
        JSONObject json = new JSONObject();
        json.put("cid", courseTeacher.getCid());
        json.put("ccredit", courseTeacher.getCcredit());
        json.put("cname", s);
        Log.i("te", "changeCourse: " + courseTeacher.getCid() + courseTeacher.getCcredit() + courseTeacher.getCname());
        new Thread() {
            @Override
            public void run() {
                super.run();
                OkHttpClient client = new OkHttpClient().newBuilder()
                        .build();
                RequestBody body = RequestBody.create(JSON, json.toString());
                Request request = new Request.Builder()
                        .url(localUrl + "course/updateCourse")
                        .method("POST", body)
                        .addHeader("Content-Type", "application/json")
                        .build();
                try {
                    Response response = client.newCall(request).execute();
                    if (response.isSuccessful()) {
//                      Log.d(TAG, "run: "+response.body().string());
                        Boolean insert_true = Boolean.parseBoolean(response.body().string());
                        final MainActivity mainActivity;
                        mainActivity = (MainActivity) context;
                        if (insert_true) {
                            Handler handler = mainActivity.getHandler_main_activity();
                            Message message = handler.obtainMessage();
                            message.what = RequestConstant.REQUEST_SUCCESS;
                            handler.sendMessage(message);
                        } else {
                            Handler handler = mainActivity.getHandler_main_activity();
                            Message message = handler.obtainMessage();
                            message.what = RequestConstant.REQUEST_FAILURE;
                            handler.sendMessage(message);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    //确定删除课程
    private void enSureDelete(CourseTeacher courseTeacher) {
        Log.d(TAG, "enSureDelete: " + courseTeacher.toString());
        StudentManagerApplication application = (StudentManagerApplication) context.getApplicationContext();
        String url = localUrl + "course/deleteById/" + courseTeacher.getCid();
        new Thread() {
            @Override
            public void run() {
                super.run();
                OkHttpClient client = new OkHttpClient().newBuilder()
                        .build();
                Request request = new Request.Builder()
                        .url(url)
                        .method("GET", null)
                        .build();
                try {
                    Response response = client.newCall(request).execute();
                    if (response.isSuccessful()) {
//                      Log.d(TAG, "run: "+response.body().string());
                        Boolean insert_true = Boolean.parseBoolean(response.body().string());
                        final MainActivity mainActivity;
                        mainActivity = (MainActivity) context;
                        if (insert_true) {
                            Handler handler = mainActivity.getHandler_main_activity();
                            Message message = handler.obtainMessage();
                            message.what = RequestConstant.REQUEST_SUCCESS;
                            handler.sendMessage(message);
                        } else {
                            Handler handler = mainActivity.getHandler_main_activity();
                            Message message = handler.obtainMessage();
                            message.what = RequestConstant.REQUEST_FAILURE;
                            handler.sendMessage(message);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    //获取数据数量
    @Override
    public int getItemCount() {
        return courseManageList.size();
    }

    //实时更新数据
    public void updateCourseList(ArrayList<CourseTeacher> mcoursesList) {
        this.courseManageList.clear();
        this.courseManageList.addAll(mcoursesList);
        notifyDataSetChanged();
    }

    //实例化控件
    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView course_credit_ccredit, course_name_cname, course_id_cid;
        Button button;

        //绑定指定控件
        public ViewHolder(View itemView) {
            super(itemView);
            course_id_cid = itemView.findViewById(R.id.course_manage_id_cid);
            course_name_cname = itemView.findViewById(R.id.course_manage_name_cname);
            course_credit_ccredit = itemView.findViewById(R.id.course_manage_credit_ccredit);
        }
    }
}
