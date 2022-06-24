package com.shu.studentmanager.fragment;

import static com.shu.studentmanager.activity.LoginActivity.localUrl;
import static com.shu.studentmanager.constant.DurationsKt.MEDIUM_EXPAND_DURATION;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.Transition;
import androidx.transition.TransitionManager;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.shu.studentmanager.R;
import com.shu.studentmanager.StudentManagerApplication;
import com.shu.studentmanager.adpater.StudentCourseAdapter;
import com.shu.studentmanager.databinding.StudentFragmentBinding;
import com.shu.studentmanager.entity.CourseStudent;
import com.shu.studentmanager.entity.Student;
import com.shu.studentmanager.transition.TransitionsKt;
import com.shu.studentmanager.viewmodel.StudentViewModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class StudentFragment extends Fragment {

    private StudentViewModel studentViewModel;
    public static StudentFragmentBinding studentFragmentBinding;
    private TextInputEditText mtextInputEditText, mtextInputEditTextagain;
    private TextInputLayout mtextInputLayout;
    private TextView mtextViewName, mtextViewId;
    Handler handler;
    Context context;

    public static StudentFragment newInstance() {
        return new StudentFragment();
    }

    private RecyclerView student_course_list_recyclerview;
    private StudentCourseAdapter studentCourseAdapter;

    String newPassword;
    String newPassword2;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // 创建 DataViewModel 对象，让 DataViewModel 和 Activity 进行绑定，通过反射拿到对象
        studentViewModel = new ViewModelProvider(this).get(StudentViewModel.class);

        //绑定布局页面
        studentFragmentBinding = DataBindingUtil.inflate(inflater, R.layout.student_fragment, container, false);

        //将布局页面信息set到studentFragmentBinding
        studentFragmentBinding.setStudentViewModel(studentViewModel);

        //开启生命周期活动
        studentFragmentBinding.setLifecycleOwner(getActivity());

        //创建一个类的全局变量
        StudentManagerApplication application = (StudentManagerApplication) getActivity().getApplication();

        //显示主页的id和名字
        studentFragmentBinding.studentFragmentStudentId.setText(application.getId());
        studentFragmentBinding.studentFragmentStudentName.setText(application.getName());

        //显示列表的数据
        student_course_list_recyclerview = studentFragmentBinding.studentFragmentCourseListRecycleview;
        setStudentCoureListRecycleView();
        View root = studentFragmentBinding.getRoot();

        try {
            initStudentCourseList();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return root;
//        return inflater.inflate(R.layout.student_fragment, container, false);
    }

    //显示主页数据
    private void setStudentCoureListRecycleView() {
        //设置recyclerview的宽和高自适应
        student_course_list_recyclerview.setHasFixedSize(true);
//        设置recyclerview的布局方式
        student_course_list_recyclerview.setLayoutManager(new LinearLayoutManager(getActivity()));
//        与适配器交互
        studentCourseAdapter = new StudentCourseAdapter(getActivity());
        student_course_list_recyclerview.setAdapter(studentCourseAdapter);
//        viewModel数据传输
        studentViewModel.getMutableLiveData_student_course_list().observe(getActivity(), new Observer<ArrayList<CourseStudent>>() {
            @Override
            public void onChanged(ArrayList<CourseStudent> courseStudents) {
//                Log.d(TAG, "onChanged: update");
                studentFragmentBinding.studentFragmentStudentClassNumber.setText("本学期共有 " + String.valueOf(courseStudents.size()) + " 门课");
                studentCourseAdapter.updateCourseList(courseStudents);
            }
        });
    }

    //通过id获取学生的课程和成绩
    private void initStudentCourseList() throws IOException {
        StudentManagerApplication application = (StudentManagerApplication) getActivity().getApplication();
        String url = localUrl + "SCT/findBySid/" + application.getId() + "/" + application.getCurrentTerm();
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
//                    Log.d(TAG, "run: "+response.body().string());
                    if (response.isSuccessful()) {
                        ArrayList<CourseStudent> tlist = new Gson().fromJson(response.body().string(), new TypeToken<ArrayList<CourseStudent>>() {
                        }.getType());
//                        mlist.addAll(tlist);
                        studentViewModel.setMutableLiveData_student_course_list(tlist);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();

    }

    @Override
    public void onResume() {
//        mViewModel = new ViewModelProvider(this).get(TeacherViewModel.class);
        // TODO: Use the ViewModel
        mtextInputEditText = getActivity().findViewById(R.id.student_fragment_new_password_1);
        mtextInputEditTextagain = getActivity().findViewById(R.id.student_fragment_new_password2);
        mtextViewName = getActivity().findViewById(R.id.student_fragment_student_name);
        mtextViewId = getActivity().findViewById(R.id.student_fragment_student_id);

        super.onResume();

//        点击轻触查看个人信息显示修改密码页面并获取到数据
        studentFragmentBinding.studentFragmentTouchToEditBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TransAnimationGo();
                String sname = String.valueOf(mtextViewName.getText());
                String sid = String.valueOf(mtextViewId.getText());
            }
        });

//        点击提交修改密码
        studentFragmentBinding.studentFragmentTouchToResetPasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Student student = new Student();
                newPassword = mtextInputEditText.getText().toString();
                newPassword2 = mtextInputEditTextagain.getText().toString();
                Log.i("a", "newPassword: " + newPassword);
                Log.i("a", "newPassword2: " + newPassword2);
                String sname = String.valueOf(mtextViewName.getText());
                String sid = String.valueOf(mtextViewId.getText());
                student.setSid(sid);
                student.setPassword(newPassword);
                student.setSname(sname);
//                判断两次密码是否一致
                if (newPassword.equals("") || newPassword2.equals("") || newPassword.equals(" ") || newPassword2.equals(" ")) {
                    Toast.makeText(v.getContext(), "密码不能为空", Toast.LENGTH_SHORT).show();
                } else if (newPassword.equals(newPassword2)) {
                    try {
                        changePassword(student);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    TransAnimationBack();
                    Toast.makeText(v.getContext(), "密码修改成功", Toast.LENGTH_SHORT).show();
                } else if (!(newPassword).equals(newPassword2)) {
                    Toast.makeText(v.getContext(), "密码不一致", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(v.getContext(), "未知错误", Toast.LENGTH_SHORT).show();
                }

            }
        });
        try {
            initStudentCourseList();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //修改密码
    private void changePassword(Student student) throws JSONException {
        MediaType JSON = MediaType.parse("application/json;charset=utf-8");
        JSONObject json = new JSONObject();
        json.put("sid", student.getSid());
        json.put("sname", student.getSname());
        json.put("password", student.getPassword());
        String url = localUrl + "student/updateStudent";

        new Thread() {
            @Override
            public void run() {
                super.run();
                OkHttpClient client = new OkHttpClient().newBuilder()
                        .build();
                RequestBody body = RequestBody.create(JSON, json.toString());
                Request request = new Request.Builder()
                        .url(url)
                        .method("POST", body)
                        .addHeader("Content-Type", "application/json")
                        .build();
                try {
                    Response response = client.newCall(request).execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();

    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        studentFragmentBinding = null;
    }

    //点击切换到修改密码
    public void TransAnimationGo() {
        Transition transition = TransitionsKt.fadeThrough().setDuration(MEDIUM_EXPAND_DURATION);
        TransitionManager.beginDelayedTransition(studentFragmentBinding.studentFragmentMoveConstrainParent, transition);
        studentFragmentBinding.studentFragmentResetUserPassword.setVisibility(View.VISIBLE);
        studentFragmentBinding.studentFragmentUserInformation.setVisibility(View.GONE);
    }

    //点击切换到个人信息
    public void TransAnimationBack() {
        Transition transition = TransitionsKt.fadeThrough().setDuration(MEDIUM_EXPAND_DURATION);
        TransitionManager.beginDelayedTransition(studentFragmentBinding.studentFragmentMoveConstrainParent, transition);
        studentFragmentBinding.studentFragmentResetUserPassword.setVisibility(View.GONE);
        studentFragmentBinding.studentFragmentUserInformation.setVisibility(View.VISIBLE);
    }

}