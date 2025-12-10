package com.example.final_project.ui.tracnghiem;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.final_project.R;
import com.example.final_project.data.model.Question;
import com.example.final_project.data.repository.QuestionRepository;
import com.example.final_project.ui.ketqua.KetQuaTracNghiemActivity;

import java.util.List;

public class ChonTracNghiemActivity extends AppCompatActivity {

    private TextView textCauHoi, textTienDo;
    private LinearLayout answer1, answer2, answer3, answer4, progressBar, btnNext;

    private List<Question> questions;
    private int currentIndex = 0;
    private int totalScore = 0;
    private int selectedScore = -1;
    private LinearLayout fullTiendO;
    private int fullWidth = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acitivity_chon_tracnghiem);

        initViews();
        loadQuestions();
        fullTiendO.post(() -> {
            fullWidth = fullTiendO.getWidth();  // Lấy chiều rộng thật
        });

    }

    private void initViews() {
        textCauHoi = findViewById(R.id.textCauHoi);
        textTienDo = findViewById(R.id.texttiendo);
        progressBar = findViewById(R.id.thanhtiendo);

        answer1 = findViewById(R.id.btnanswer1);
        answer2 = findViewById(R.id.btnanswer2);
        answer3 = findViewById(R.id.btnanswer3);
        answer4 = findViewById(R.id.btnanswer4);
        btnNext = findViewById(R.id.btnNext);
        fullTiendO = findViewById(R.id.fulltiendo);

    }

    private void loadQuestions() {
        new QuestionRepository().loadRandomQuestions(new QuestionRepository.QuestionCallback() {
            @Override
            public void onSuccess(List<Question> randomQuestions) {
                questions = randomQuestions;
                showQuestion();
            }

            @Override
            public void onFail(String error) {
                textCauHoi.setText("Lỗi tải câu hỏi: " + error);
            }
        });
    }

    private void showQuestion() {

        if (questions == null || questions.isEmpty()) {
            textCauHoi.setText("Không có dữ liệu câu hỏi");
            return;
        }

        selectedScore = -1;
        Question q = questions.get(currentIndex);

        textCauHoi.setText(q.getText());
        textTienDo.setText("Câu " + (currentIndex + 1) + "/9");

        if (fullWidth > 0) {
            int progressWidth = (int) (((float) (currentIndex + 1) / 9) * fullWidth);
            progressBar.getLayoutParams().width = progressWidth;
            progressBar.requestLayout();
        }


        ((TextView) answer1.findViewById(R.id.rkfgxuhfnj)).setText(q.getAnswers().get(0).getText());
        ((TextView) answer2.findViewById(R.id.ri1layallfa)).setText(q.getAnswers().get(1).getText());
        ((TextView) answer3.findViewById(R.id.rml03nipvo3c)).setText(q.getAnswers().get(2).getText());
        ((TextView) answer4.findViewById(R.id.rsxnewt2ggn9)).setText(q.getAnswers().get(3).getText());

        answer1.setOnClickListener(v -> selectedScore = q.getAnswers().get(0).getScore());
        answer2.setOnClickListener(v -> selectedScore = q.getAnswers().get(1).getScore());
        answer3.setOnClickListener(v -> selectedScore = q.getAnswers().get(2).getScore());
        answer4.setOnClickListener(v -> selectedScore = q.getAnswers().get(3).getScore());

        btnNext.setOnClickListener(v -> {
            if (selectedScore == -1) return;

            totalScore += selectedScore;
            currentIndex++;

            if (currentIndex >= 9) {
                goToResult();
                return;
            }
            showQuestion();
        });
    }

    private void goToResult() {
        Intent intent = new Intent(ChonTracNghiemActivity.this, KetQuaTracNghiemActivity.class);
        intent.putExtra("score", totalScore);
        startActivity(intent);
        finish();
    }
}
