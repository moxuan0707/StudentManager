package com.shu.studentmanager.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.shu.studentmanager.R;
import com.shu.studentmanager.StudentManagerApplication;
import com.shu.studentmanager.constant.RequestConstant;
import com.shu.studentmanager.databinding.ActivityLoginBinding;
import com.shu.studentmanager.entity.Student;
import com.shu.studentmanager.entity.Teacher;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class LoginActivity extends AppCompatActivity {
    public static String localUrl = "http://10.63.55.21:10086/";
    private static final String TAG = "LoginActivity";
    private ActivityLoginBinding activityLoginBinding;
    private Button btn_login;
    private TextInputEditText text_username;
    private TextInputEditText text_password;
    private TextInputEditText password_see;
    private RadioGroup radioGroup;
    private String user_kind;
    Handler handler;

    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_login);

        //用databinding代替传统绑定布局方式
        activityLoginBinding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(activityLoginBinding.getRoot());
        btn_login = activityLoginBinding.logInBtn;
        text_username = activityLoginBinding.usernameText;
        text_password = activityLoginBinding.passwordText;
        radioGroup = activityLoginBinding.selectKindUser;
//        登录的handler进程
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == RequestConstant.REQUEST_SUCCESS) {
//                    Intent跳转
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.putExtras(msg.getData());
                    startActivity(intent);
                    finish();
                }
            }
        };
        initListener();

        //保证密码初始时候是隐藏的
        text_password.setTransformationMethod(PasswordTransformationMethod.getInstance());
    }

    //登录按钮点击事件
    private void initListener() {
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
//                    获取输入的账号和密码
                    login(text_username.getText().toString(), text_password.getText().toString());
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }
        });
//        学生和教师选择按钮
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.select_teacher:
                        user_kind = "teacher";
                        break;
                    case R.id.select_student:
                        user_kind = "student";
                        break;
                    default:
                        break;
                }
            }
        });
    }

    //    创建登录的线程
    private void login(String username, String password) throws IOException, JSONException {
//        Log.d(TAG, "login: "+user_kind);
        MediaType JSON = MediaType.parse("application/json;charset=utf-8");
        JSONObject json = new JSONObject();
//        判断单选框选择的是什么就进行什么登录（根据user_kind改变请求URL）
        if (user_kind == "teacher") {
            json.put("tid", username);
        } else if (user_kind == "student") {
            json.put("sid", username);
        } else {
            Toast.makeText(this, "请选择用户类型！！！", Toast.LENGTH_SHORT).show();
        }
        json.put("password", password);
        String url = localUrl + user_kind + "/login";
//        线程开始
        new Thread() {
            @Override
            public void run() {
                super.run();
                Message message = handler.obtainMessage();
                Bundle bundle = new Bundle();
                OkHttpClient client = new OkHttpClient().newBuilder()
                        .build();
                MediaType mediaType = MediaType.parse("application/json");
                RequestBody body = RequestBody.create(JSON, json.toString());
                Request request = new Request.Builder()
                        .url(url)
                        .method("POST", body)
                        .addHeader("Content-Type", "application/json;charset=utf-8")
                        .build();
                Response response = null;
                try {
                    response = client.newCall(request).execute();
                    if (response.isSuccessful()) {
//                      Log.d(TAG, "run: "+response.body().string());
                        Boolean login_true = Boolean.parseBoolean(response.body().string());

                        if (login_true) {
                            Log.d(TAG, "UserLogin: success");
                            message.what = RequestConstant.REQUEST_SUCCESS;
                            bundle.putString("UserKind", user_kind);
                            load(username);
                        } else {
//                          安卓提示框 Toast 在子线程使用，直接使用 makeTest().show();不显示，需要如下
                            Looper.prepare();
                            Toast.makeText(LoginActivity.this, "账号或密码错误,请重新输入!", Toast.LENGTH_LONG).show();
                            Looper.loop();
                            message.what = RequestConstant.REQUEST_FAILURE;
                        }

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                message.setData(bundle);
                handler.sendMessage(message);
            }
        }.start();
    }

    //加载数据
    private void load(String username) throws IOException {
        String url = localUrl + user_kind + "/findById/" + username;
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        Request request = new Request.Builder()
                .url(url)
                .method("GET", null)
                .build();
        Response response = client.newCall(request).execute();
        if (response.isSuccessful()) {
            if (user_kind == "student") {
                Student student = new Gson().fromJson(response.body().string(), Student.class);
                Log.d(TAG, "load: " + student.toString());
                StudentManagerApplication application = (StudentManagerApplication) getApplication();
                application.setId(student.getSid());
                application.setName(student.getSname());
                application.setToken(true);
                application.setType("student");
            } else if (user_kind == "teacher") {
                Teacher teacher = new Gson().fromJson(response.body().string(), Teacher.class);
                Log.d(TAG, "load: " + teacher.toString());
                StudentManagerApplication application = (StudentManagerApplication) getApplication();
                application.setId(teacher.getTid());
                application.setName(teacher.getTname());
                application.setToken(true);
                application.setType("teacher");
            }
        }
        load_select_ok();
        load_current_term();
    }

    //获取学期信息
    private void load_select_ok() throws IOException {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        Request request = new Request.Builder()
                .url(localUrl + "info/getCurrentTerm")
                .method("GET", null)
                .build();
        Response response = client.newCall(request).execute();
        if (response.isSuccessful()) {
//                      Log.d(TAG, "run: "+response.body().string());
            Boolean forbidCourseSelection = Boolean.parseBoolean(response.body().string());
            StudentManagerApplication application = (StudentManagerApplication) getApplication();
            application.setForbidCourseSelection(forbidCourseSelection);
        }
    }

    private void load_current_term() throws IOException {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        Request request = new Request.Builder()
                .url(localUrl + "info/getCurrentTerm")
                .method("GET", null)
                .build();
        Response response = client.newCall(request).execute();
        if (response.isSuccessful()) {
//                      Log.d(TAG, "run: "+response.body().string());
//            Boolean forbidCourseSelection = Boolean.parseBoolean(response.body().string());
            StudentManagerApplication application = (StudentManagerApplication) getApplication();
            application.setCurrentTerm(response.body().string());
        }
    }
}