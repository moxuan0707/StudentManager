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
import com.shu.studentmanager.entity.Student;
import com.shu.studentmanager.entity.Teacher;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class TeacherAdapter extends RecyclerView.Adapter<TeacherAdapter.ViewHolder> {
    Context context;
    ArrayList<Teacher> teacherList;

    public TeacherAdapter() {
        this.teacherList = new ArrayList<Teacher>();
    }

    public TeacherAdapter(Context context) {
        this.context = context;
        this.teacherList = new ArrayList<Teacher>();
    }

    public TeacherAdapter(Context context, ArrayList<Teacher> teacherList) {
        this.context = context;
        this.teacherList = teacherList;
    }

    //这个方法主要是用于找到子项item得布局，其中的代码基本上都是上面两行代码，
    // 需要从activity中传一个context上下文过来.
    @NonNull
    @Override
    public TeacherAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_teacher, parent, false);
        return new TeacherAdapter.ViewHolder(view);
    }

    //负责每个子项目的holder数据绑定
    @Override
    public void onBindViewHolder(@NonNull TeacherAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        if (teacherList != null && teacherList.size() > 0) {
            Teacher teacherEntity = teacherList.get(position);
            holder.teacher_id.setText(teacherEntity.getTid());
            holder.teacher_name.setText(teacherEntity.getTname());
            holder.teacher_password.setText(teacherEntity.getPassword());
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.P)
                @Override
                public void onClick(View view) {
                    showAlertDialogMode(position, holder.teacher_password);
                }
            });
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    showAlertDialoDelete(position);
                    return true;
                }
            });
        }
    }

    private void showAlertDialoDelete(int position) {
        new MaterialAlertDialogBuilder(context)
                .setTitle("确认")
                .setMessage("确定删除该学生？")
                .setNeutralButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.i("re", "onClick: " + which + " " + position);
                        enSureDelete(teacherList.get(position));
                        teacherList.remove(teacherList.get(position));
//                        refreshListView();
//                        刷新数据列表
                        notifyDataSetChanged();
                    }
                }).show();
    }

    private void enSureDelete(Teacher teacher) {
        Log.d(TAG, "enSureDelete: " + teacher.toString());
        String url = localUrl + "teacher/deleteById/" + teacher.getTid();
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

    //    管理员修改学生密码
    @RequiresApi(api = Build.VERSION_CODES.P)
    private void showAlertDialogMode(int position, TextView teacher_password) {
        final EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setLineHeight(12);
        FrameLayout container = new FrameLayout(context);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.leftMargin = context.getResources().getDimensionPixelSize(R.dimen.fab_margin);
        params.rightMargin = context.getResources().getDimensionPixelSize(R.dimen.fab_margin);
        input.setLayoutParams(params);
        container.addView(input);
        new MaterialAlertDialogBuilder(context)
                .setTitle("修改教师用户密码")
                .setMessage("输入新密码：")
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
                            enSureChangePassword(input.getText().toString(), teacherList.get(position));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        teacher_password.setText(input.getText().toString());
                    }
                }).show();
    }

    private void enSureChangePassword(String s, Teacher teacher) throws JSONException {
        MediaType JSON = MediaType.parse("application/json;charset=utf-8");
        JSONObject json = new JSONObject();
        json.put("tid", teacher.getTid());
        json.put("password", s);
        json.put("tname", teacher.getTname());
        new Thread() {
            @Override
            public void run() {
                super.run();
                OkHttpClient client = new OkHttpClient().newBuilder()
                        .build();
                RequestBody body = RequestBody.create(JSON, json.toString());
                Request request = new Request.Builder()
                        .url(localUrl + "teacher/updateTeacher")
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

    @Override
    public int getItemCount() {
        return teacherList.size();
    }

    public void updateStudentList(ArrayList<Teacher> mTeacherList) {
        this.teacherList.clear();
        this.teacherList.addAll(mTeacherList);
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView teacher_id, teacher_name, teacher_password;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            teacher_id = itemView.findViewById(R.id.manage_teacher_id_tid);
            teacher_name = itemView.findViewById(R.id.manage_teacher_name_tname);
            teacher_password = itemView.findViewById(R.id.manage_teacher_password_tpassword);
        }
    }
}
