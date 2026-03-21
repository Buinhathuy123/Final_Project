package com.example.final_project.ui.tracnghiem;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.final_project.R;
import com.example.final_project.ui.ketqua.KetQuaTracNghiemActivity;
import com.example.final_project.data.model.Question;
import com.example.final_project.data.model.Answer;
import java.util.Arrays;
import java.util.List;

public class ChonTracNghiemActivity extends AppCompatActivity {

    private TextView textCauHoi, textTienDo;

    private LinearLayout answer1, answer2, answer3, answer4;
    private LinearLayout btnNext, fullTiendO;

    private View progressBar;

    private ImageView cb1, cb2, cb3, cb4;

    private List<Question> questions;

    private int currentIndex = 0;
    private int totalScore = 0;
    private int selectedScore = -1;

    private int fullWidth = 0;

    // ================= LIFECYCLE =================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acitivity_chon_tracnghiem);

        initViews();
        loadLocalQuestions();

        fullTiendO.post(() -> fullWidth = fullTiendO.getWidth());
    }

    // ================= INIT UI =================
    private void initViews() {

        textCauHoi = findViewById(R.id.textCauHoi);
        textTienDo = findViewById(R.id.texttiendo);

        progressBar = findViewById(R.id.thanhtiendo);
        fullTiendO = findViewById(R.id.fulltiendo);

        answer1 = findViewById(R.id.btnanswer1);
        answer2 = findViewById(R.id.btnanswer2);
        answer3 = findViewById(R.id.btnanswer3);
        answer4 = findViewById(R.id.btnanswer4);

        btnNext = findViewById(R.id.btnNext);

        cb1 = findViewById(R.id.rfhcopeyua74);
        cb2 = findViewById(R.id.rn2sqq8hs6s);
        cb3 = findViewById(R.id.r2h3dli005i);
        cb4 = findViewById(R.id.r1y9q5zgzvp4);
    }

    // ================= LOCAL QUESTIONS =================
    private void loadLocalQuestions() {

        questions = Arrays.asList(

                new Question("Ít hứng thú hoặc không còn thích làm những việc trước đây bạn từng thích?",
                        Arrays.asList(
                                new Answer("Không bao giờ",0),
                                new Answer("Thỉnh thoảng",1),
                                new Answer("Thường xuyên",2),
                                new Answer("Luôn luôn",3)
                        )),

                new Question("Cảm thấy buồn bã, chán nản hoặc tuyệt vọng?",
                        Arrays.asList(
                                new Answer("Không bao giờ",0),
                                new Answer("Thỉnh thoảng",1),
                                new Answer("Thường xuyên",2),
                                new Answer("Luôn luôn",3)
                        )),

                new Question("Khó ngủ hoặc ngủ không sâu giấc?",
                        Arrays.asList(
                                new Answer("Không bao giờ",0),
                                new Answer("Thỉnh thoảng",1),
                                new Answer("Thường xuyên",2),
                                new Answer("Luôn luôn",3)
                        )),

                new Question("Cảm thấy mệt mỏi hoặc thiếu năng lượng?",
                        Arrays.asList(
                                new Answer("Không bao giờ",0),
                                new Answer("Thỉnh thoảng",1),
                                new Answer("Thường xuyên",2),
                                new Answer("Luôn luôn",3)
                        )),

                new Question("Ăn uống kém hoặc ăn quá nhiều?",
                        Arrays.asList(
                                new Answer("Không bao giờ",0),
                                new Answer("Thỉnh thoảng",1),
                                new Answer("Thường xuyên",2),
                                new Answer("Luôn luôn",3)
                        )),

                new Question("Cảm thấy bản thân tệ hoặc thất bại?",
                        Arrays.asList(
                                new Answer("Không bao giờ",0),
                                new Answer("Thỉnh thoảng",1),
                                new Answer("Thường xuyên",2),
                                new Answer("Luôn luôn",3)
                        )),

                new Question("Khó tập trung vào việc đọc, làm việc hoặc học tập?",
                        Arrays.asList(
                                new Answer("Không bao giờ",0),
                                new Answer("Thỉnh thoảng",1),
                                new Answer("Thường xuyên",2),
                                new Answer("Luôn luôn",3)
                        )),

                new Question("Di chuyển hoặc nói chuyện chậm hơn bình thường?",
                        Arrays.asList(
                                new Answer("Không bao giờ",0),
                                new Answer("Thỉnh thoảng",1),
                                new Answer("Thường xuyên",2),
                                new Answer("Luôn luôn",3)
                        )),

                new Question("Cảm thấy lo lắng hoặc bồn chồn?",
                        Arrays.asList(
                                new Answer("Không bao giờ",0),
                                new Answer("Thỉnh thoảng",1),
                                new Answer("Thường xuyên",2),
                                new Answer("Luôn luôn",3)
                        )),

                new Question("Cảm thấy cuộc sống không còn nhiều ý nghĩa?",
                        Arrays.asList(
                                new Answer("Không bao giờ",0),
                                new Answer("Thỉnh thoảng",1),
                                new Answer("Thường xuyên",2),
                                new Answer("Luôn luôn",3)
                        ))
        );

        currentIndex = 0;
        totalScore = 0;

        showQuestion();
    }

    // ================= SHOW QUESTION =================
    private void showQuestion() {

        if (currentIndex >= questions.size()) {
            goToResult();
            return;
        }

        selectedScore = -1;
        resetSelection();

        Question q = questions.get(currentIndex);

        textCauHoi.setText(q.getText());
        textTienDo.setText("Câu " + (currentIndex + 1) + "/" + questions.size());

        if (fullWidth > 0) {

            int progressWidth =
                    (int)(((float)(currentIndex + 1) / questions.size()) * fullWidth);

            progressBar.getLayoutParams().width = progressWidth;
            progressBar.requestLayout();
        }

        ((TextView)answer1.findViewById(R.id.rkfgxuhfnj))
                .setText(q.getAnswers().get(0).getText());

        ((TextView)answer2.findViewById(R.id.ri1layallfa))
                .setText(q.getAnswers().get(1).getText());

        ((TextView)answer3.findViewById(R.id.rml03nipvo3c))
                .setText(q.getAnswers().get(2).getText());

        ((TextView)answer4.findViewById(R.id.rsxnewt2ggn9))
                .setText(q.getAnswers().get(3).getText());

        answer1.setOnClickListener(v -> selectAnswer(0,q));
        answer2.setOnClickListener(v -> selectAnswer(1,q));
        answer3.setOnClickListener(v -> selectAnswer(2,q));
        answer4.setOnClickListener(v -> selectAnswer(3,q));

        btnNext.setOnClickListener(v -> {

            if (selectedScore == -1) return;

            totalScore += selectedScore;
            currentIndex++;

            showQuestion();
        });
    }

    // ================= ANSWER =================
    private void selectAnswer(int index, Question q) {

        resetSelection();

        selectedScore = q.getAnswers().get(index).getScore();

        switch(index){

            case 0:
                cb1.setSelected(true);
                break;

            case 1:
                cb2.setSelected(true);
                break;

            case 2:
                cb3.setSelected(true);
                break;

            case 3:
                cb4.setSelected(true);
                break;
        }
    }
    private void resetSelection(){

        cb1.setSelected(false);
        cb2.setSelected(false);
        cb3.setSelected(false);
        cb4.setSelected(false);
    }

    // ================= RESULT =================
    private void goToResult(){

        Intent intent =
                new Intent(this, KetQuaTracNghiemActivity.class);

        intent.putExtra("score", totalScore);

        startActivity(intent);
        finish();
    }
}