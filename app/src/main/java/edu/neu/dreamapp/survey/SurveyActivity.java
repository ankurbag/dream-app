package edu.neu.dreamapp.survey;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import edu.neu.dreamapp.MainActivity;
import edu.neu.dreamapp.R;
import edu.neu.dreamapp.base.BaseActivity;
import edu.neu.dreamapp.model.SurveyQuestion;
import edu.neu.dreamapp.widget.CustomProgressBar;

/**
 * @author agrawroh
 * @version v1.0
 */
public class SurveyActivity extends BaseActivity implements SurveyFragment.SelectionCallback {

    @BindView(R.id.ivBack)
    ImageView ivBack;

    @BindView(R.id.tvTitle)
    TextView tvTitle;

    @BindView(R.id.horizontalProgress)
    CustomProgressBar horizontalProgress;

    @BindView(R.id.btnPrevious)
    Button btnPrevious;

    @BindView(R.id.btnNext)
    Button btnNext;

    private List<SurveyQuestion> surveyQuestions;
    private int progress;
    private SurveyFragment surveyFragment;

    @Override
    protected String getTAG() {
        return this.toString();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_survey;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
    }

    /**
     * Initialize Views
     */
    @Override
    protected void initResAndListener() {
        ivBack.setVisibility(View.VISIBLE);
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initEndDialog();
            }
        });

        tvTitle.setText(getResources().getString(R.string.question));

        /* Grab Fragment */
        surveyFragment = (SurveyFragment) getSupportFragmentManager().findFragmentById(R.id.surveyFragment);
        surveyFragment.setSelectionCallback(this);

        btnNext.setEnabled(false);

        /* Next Question Button */
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (progress == surveyQuestions.size() - 1) {
                    pushAnswersOnFirebase();
                } else {
                    progress++;
                    surveyFragment.nextQuestion(progress, surveyQuestions.get(progress));
                    horizontalProgress.setProgress((int) (((double) progress / (double) surveyQuestions.size()) * 100));

                /* Enable previous button since there is always a question to go back to */
                    btnPrevious.setEnabled(true);
                }
                btnNext.setEnabled(isChecked(progress));
            }
        });

        /* Previous Question Button */
        btnPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progress--;
                surveyFragment.previousQuestion(progress, surveyQuestions.get(progress));
                horizontalProgress.setProgress((int) (((double) progress / (double) surveyQuestions.size()) * 100));

                /* Disable previous button if progress is pushed back to 0 */
                if (progress == 0) {
                    btnPrevious.setEnabled(false);
                }
                btnNext.setEnabled(isChecked(progress));
            }
        });

        initQuestion();
    }

    /**
     * Initializing Survey Questions (Pre-entered)
     */
    private void initQuestion() {
        surveyQuestions = new ArrayList<>();
        /* Attendance */
        surveyQuestions.add(new SurveyQuestion("Attendance", "",
                Arrays.asList("Peter", "Roger", "Harry", "Charlie")));

        /* Questions */
        surveyQuestions.add(new SurveyQuestion("How would you rate your overall health and wellness?", "",
                Arrays.asList("Excellent", "Good", "Fair", "Poor")));
        surveyQuestions.add(new SurveyQuestion("Counting sessions of at least 10 minutes, how many minutes of accumulated physical activity do you participate in weekly?", "",
                Arrays.asList("I am not physically active", "less than 75 minutes", "75 - 149 minutes", "150 - 300 minutes", "more than 300 minutes")));
        surveyQuestions.add(new SurveyQuestion("How many days per week do you participate in stretching exercises?", "",
                Arrays.asList("I do not stretch often, if ever", "1 day/week", "2 days/week", "3 or more days/week")));
        surveyQuestions.add(new SurveyQuestion("How many days each week do you participate in strength-building or resistance training exercises? Each session needs to be at least 10 minutes in length.", "",
                Arrays.asList("I do not typically weight-lift or resistance train", "1 day/week", "2 days/week", "3 or more days/week")));

        /* Set current Progress to 0 */
        progress = 0;

        /* Call nextQuestion on SurveyFragment to load out the first question */
        surveyFragment.nextQuestion(progress, surveyQuestions.get(progress));

        /* Set current Progressbar to show 0 */
        horizontalProgress.setProgress(0);

        /* Disable previous button since it is the first question */
        btnPrevious.setEnabled(false);
    }

    /**
     * Overwrites the interface and creates callback from SurveyFragment
     *
     * @param index     Index Location
     * @param selection Selection
     */
    @Override
    public void itemSelected(int index, String selection) {
        surveyQuestions.get(index).setSelected(selection);
        if (0 == index) {
            List<String> options = new ArrayList<>();
            int valPresent = 0;
            for (final String name : surveyQuestions.get(0).getOption()) {
                if (Arrays.asList(selection.substring(1, selection.length() - 1).split(", ")).contains(String.valueOf(valPresent))) {
                    options.add(name + " : Yes?");
                }
                ++valPresent;
            }
            for (int i = 1; i < surveyQuestions.size(); i++) {
                surveyQuestions.get(i).setOption(options);
            }
        }
        btnNext.setEnabled(true);
    }

    public void pushAnswersOnFirebase() {
        /*
        FirebaseDatabase.getInstance().getReference().child("survey").orderByChild("id")
                .equalTo(AppUtils.createPathString(UserSingleton.USERINFO.getId()))
                .limitToFirst(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    SurveyAnswer surveyAnswer = new SurveyAnswer(AppUtils.createPathString(UserSingleton.USERINFO.getId()), surveyQuestions);
                    FirebaseDatabase.getInstance().getReference().child("survey").push().setValue(surveyAnswer);
                }

                startActivity(new Intent(mContext, MainActivity.class));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        */
    }

    /**
     * return if any of the options are selected
     *
     * @return Return Value
     */
    public boolean isChecked(int position) {
        return null != surveyQuestions.get(position).getSelected();
    }

    /**
     * Survey entry confirmation dialog initialization
     */
    @SuppressWarnings("all")
    private void initEndDialog() {
        View layout = LayoutInflater.from(mContext).inflate(R.layout.dialog_popup, null);
        final AlertDialog dialog = new AlertDialog.Builder(mContext).setView(layout).show();
        TextView tvContent = (TextView) layout.findViewById(R.id.tvContent);
        tvContent.setText(getResources().getString(R.string.end_survey));
        TextView tv_cancel = (TextView) layout.findViewById(R.id.tv_cancel);
        tv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        TextView tv_confirm = (TextView) layout.findViewById(R.id.tv_confirm);

        /* if confirm, launch MainActivity */
        tv_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                Intent intent = new Intent(mContext, MainActivity.class);
                ((Activity) mContext).startActivity(intent);

                finish();
                overridePendingTransition(R.anim.fade_in_500, R.anim.fade_out_500);
            }
        });
    }
}