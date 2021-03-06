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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.shu.studentmanager.R;
import com.shu.studentmanager.activity.LoginActivity;
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

public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.ViewHolder> {
    Context context;
    ArrayList<Student> studentList;

    public StudentAdapter() {
        this.studentList = new ArrayList<Student>();
    }

    public StudentAdapter(Context context) {
        this.context = context;
        this.studentList = new ArrayList<Student>();
    }

    public StudentAdapter(Context context, ArrayList<Student> studentList) {
        this.context = context;
        this.studentList = studentList;
    }

    //这个方法主要是用于找到子项item得布局，其中的代码基本上都是上面两行代码，
    // 需要从activity中传一个context上下文过来.
    @NonNull
    @Override
    public StudentAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_student, parent, false);
        return new ViewHolder(view);
    }

    //负责每个子项目的holder数据绑定
    @Override
    public void onBindViewHolder(@NonNull StudentAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        if (studentList != null && studentList.size() > 0) {
            Student studentEntity = studentList.get(position);
            holder.student_id.setText(studentEntity.getSid());
            holder.student_name.setText(studentEntity.getSname());
            holder.student_password.setText(studentEntity.getPassword());
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.P)
                @Override
                public void onClick(View view) {
                    showAlertDialogMode(position, holder.student_password);
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
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "onClick: " + which + " " + position);
                        enSureDelete(studentList.get(position));
                        studentList.remove(studentList.get(position));
//                        refreshListView();
                        notifyDataSetChanged();
                    }
                }).show();
    }

    private void enSureDelete(Student student) {
        Log.d(TAG, "enSureDelete: " + student.toString());
        String url = localUrl + "student/deleteById/" + student.getSid();
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
    private void showAlertDialogMode(int position, TextView student_password) {
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
                .setTitle("修改学生用户密码")
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
                            enSureChangePassword(input.getText().toString(), studentList.get(position));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        student_password.setText(input.getText().toString());
                    }
                }).show();
    }

    private void enSureChangePassword(String s, Student student) throws JSONException {
        MediaType JSON = MediaType.parse("application/json;charset=utf-8");
        JSONObject json = new JSONObject();
        json.put("sid",student.getSid());
        json.put("password",s);
        json.put("sname",student.getSname());
        new Thread(){
            @Override
            public void run(){
                super.run();
                OkHttpClient client = new OkHttpClient().newBuilder()
                        .build();
                RequestBody body = RequestBody.create(JSON,json.toString());
                Request request = new Request.Builder()
                        .url(localUrl+"student/updateStudent")
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
                        if(insert_true){
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
        return studentList.size();
    }

    public void updateStudentList(ArrayList<Student> mStudentList) {
        this.studentList.clear();
        this.studentList.addAll(mStudentList);
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView student_id, student_name, student_password;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            student_id = itemView.findViewById(R.id.manage_student_id_sid);
            student_name = itemView.findViewById(R.id.manage_student_name_sname);
            student_password = itemView.findViewById(R.id.manage_student_password_spassword);
        }
    }
}
