package com.shu.studentmanager.fragment;

import static com.shu.studentmanager.activity.LoginActivity.localUrl;
import static com.shu.studentmanager.constant.DurationsKt.MEDIUM_EXPAND_DURATION;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.transition.Transition;
import androidx.transition.TransitionManager;

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
import com.shu.studentmanager.databinding.AdminFragmentBinding;
import com.shu.studentmanager.entity.CourseStudent;
import com.shu.studentmanager.entity.Teacher;
import com.shu.studentmanager.transition.TransitionsKt;
import com.shu.studentmanager.viewmodel.AdminViewModel;
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

public class AdminFragment extends Fragment {

    private AdminViewModel adminViewModel;
    private AdminFragmentBinding adminFragmentBinding;

    String newPassword;
    String newPassword2;
    private TextInputEditText mtextInputEditText, mtextInputEditTextagain;
    private TextInputLayout mtextInputLayout;
    private TextView mtextViewName, mtextViewId;

    public static AdminFragment newInstance() {
        return new AdminFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        adminViewModel = new ViewModelProvider(this).get(AdminViewModel.class);
        adminFragmentBinding = DataBindingUtil.inflate(inflater, R.layout.admin_fragment, container, false);
        adminFragmentBinding.setAdminViewModel(adminViewModel);
        adminFragmentBinding.setLifecycleOwner(getActivity());

        StudentManagerApplication application = (StudentManagerApplication) getActivity().getApplication();
        adminFragmentBinding.adminFramentTeacherId.setText("ID:"+application.getId());
        adminFragmentBinding.adminFragmentTeacherName.setText("用户名:"+application.getName());

        View root = adminFragmentBinding.getRoot();
        return root;
//        return inflater.inflate(R.layout.student_fragment, container, false);
    }

    @Override
    public void onResume() {
//        mViewModel = new ViewModelProvider(this).get(TeacherViewModel.class);
        // TODO: Use the ViewModel
        mtextInputEditText = getActivity().findViewById(R.id.admin_new_password_1);
        mtextInputEditTextagain = getActivity().findViewById(R.id.admin_new_password2);
        mtextViewName = getActivity().findViewById(R.id.admin_fragment_teacher_name);
        mtextViewId = getActivity().findViewById(R.id.admin_frament_teacher_id);
        super.onResume();
        adminFragmentBinding.adminTouchToEditBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TransAnimationGo();
                String tname = String.valueOf(mtextViewName.getText());
                String tid = String.valueOf(mtextViewId.getText());
            }
        });
        adminFragmentBinding.adminTouchToResetPasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Teacher teacher = new Teacher();
                newPassword = mtextInputEditText.getText().toString();
                newPassword2 = mtextInputEditTextagain.getText().toString();
                String tname = String.valueOf(mtextViewName.getText());
                String tid = String.valueOf(mtextViewId.getText());
                teacher.setTid(tid);
                teacher.setPassword(newPassword);
                teacher.setTname(tname);
//                判断两次密码是否一致
                if (newPassword.equals("") || newPassword2.equals("") || newPassword.equals(" ") || newPassword2.equals(" ")) {
                    Toast.makeText(v.getContext(), "密码不能为空", Toast.LENGTH_SHORT).show();
                } else if (newPassword.equals(newPassword2)) {
                    try {
                        changePassword(teacher);
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
                try {
                    changePassword(teacher);
                    Log.i("m", "onClick: " + teacher);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void changePassword(Teacher teacher) throws JSONException {
        MediaType JSON = MediaType.parse("application/json;charset=utf-8");
        JSONObject json = new JSONObject();
        json.put("tid", teacher.getTid());
        json.put("tname", teacher.getTname());
        json.put("password", teacher.getPassword());
        String url = localUrl + "teacher/updateTeacher";
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
        adminFragmentBinding = null;
    }

    public void TransAnimationGo() {
        Transition transition = TransitionsKt.fadeThrough().setDuration(MEDIUM_EXPAND_DURATION);
        TransitionManager.beginDelayedTransition(adminFragmentBinding.adminMoveConstrainParent, transition);
        adminFragmentBinding.adminResetUserPassword.setVisibility(View.VISIBLE);
        adminFragmentBinding.adminUserInformation.setVisibility(View.GONE);
    }

    public void TransAnimationBack() {
        Transition transition = TransitionsKt.fadeThrough().setDuration(MEDIUM_EXPAND_DURATION);
        TransitionManager.beginDelayedTransition(adminFragmentBinding.adminMoveConstrainParent, transition);
        adminFragmentBinding.adminResetUserPassword.setVisibility(View.GONE);
        adminFragmentBinding.adminUserInformation.setVisibility(View.VISIBLE);
    }

}