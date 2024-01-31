package com.fsre.quiz_app.game;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.fsre.quiz_app.R;
import com.fsre.quiz_app.models.GameButtonState;
import com.fsre.quiz_app.models.GameQuestion;
import com.fsre.quiz_app.models.GameState;
import com.fsre.quiz_app.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Game extends AppCompatActivity {

    FirebaseAuth auth;
    FirebaseUser loggedUser;
    private DatabaseReference db;
    private GameQuestion[] questions;
    private TextView questionText;
    private TextView gamePagePoints;
    private GameQuestion nextQuestion;
    private TextView currentQuestionIndexText;
    private TextView totalNumOfQuestionsText;
    private int currentQuestionIndex = 0;
    private List<GameQuestion> shuffledQuestions;
    private Button[] allButtons;
    private Button selectedButton;
    private GameState gameState = GameState.IDLE;
    private int points;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        this.auth = FirebaseAuth.getInstance();
        this.loggedUser = this.auth.getCurrentUser();

        TextView gamePagePoints = findViewById(R.id.game_page_points);

        // Set this.points to the current user's points
        if (loggedUser != null) {
            db = FirebaseDatabase.getInstance().getReference().child("users").child(loggedUser.getUid());
            db.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if(task.isSuccessful()){
                        try {
                            User currentUser = task.getResult().getValue(User.class);
                            if (currentUser != null) {
                                gamePagePoints.setText(currentUser.points.toString());
                            }
                        }
                        catch (NullPointerException e){
                            Log.e("NoData", e.getMessage());
                        }
                    }
                }
            });
        }

        this.db = FirebaseDatabase.getInstance().getReference().child("questions");
        ConfirmationSheet confirmationSheetFragment = new ConfirmationSheet();
        ConstraintLayout backButton = findViewById(R.id.game_back_button);


        // Fetching questions and setting up
        fetchQuestions();
        showNextQuestion();
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        questionText = findViewById(R.id.game_question);
        currentQuestionIndexText = findViewById(R.id.current_question_index);
        totalNumOfQuestionsText = findViewById(R.id.total_num_of_questions);
        Button button_a = findViewById(R.id.game_option_a);
        Button button_b = findViewById(R.id.game_option_b);
        Button button_c = findViewById(R.id.game_option_c);
        Button button_d = findViewById(R.id.game_option_d);

        allButtons = new Button[]{button_a, button_b, button_c, button_d};

        for (Button button : allButtons) {
            button.setOnClickListener(onSelect(button));
        }
    }

    public View.OnClickListener onSelect(final Button clickedButton) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(gameState != GameState.PENDING_NEXT_QUESTION){
                    for (Button button : allButtons) {
                        if (button == clickedButton) {
                            selectedButton = button;
                            gameState = GameState.PENDING_CONFIRMATION;
                            changeButtonState(selectedButton, GameButtonState.IDLE);
                            showConfirmationSheetFragment();
                        } else {
                            // Other buttons, set to INACTIVE state
                            changeButtonState(button, GameButtonState.INACTIVE);
                        }
                    }
                }
            }
        };
    }

    public void onConfirmationSheetButtonClick() {
        if (gameState == GameState.PENDING_CONFIRMATION) {
            boolean isCorrect = checkAnswer(selectedButton);
            if (isCorrect) {
                changeButtonState(selectedButton, GameButtonState.CORRECT);
                updateUserPoints(10);
            } else {
                // Find the correct button and change its state to CORRECT
                int correctButtonIndex = findCorrectButtonIndex();
                if (correctButtonIndex != -1) {
                    Button correctButton = allButtons[correctButtonIndex];
                    changeButtonState(correctButton, GameButtonState.CORRECT);
                }
                changeButtonState(selectedButton, GameButtonState.INCORRECT);
                updateUserPoints(-5);
            }
            updateTextInFragment("Sljedeće pitanje");
            gameState = GameState.PENDING_NEXT_QUESTION;
        } else if (gameState == GameState.PENDING_NEXT_QUESTION) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showNextQuestion();
                    changeButtonState(selectedButton, GameButtonState.INACTIVE);
                    updateTextInFragment("Jeste li sigurni?");
                    closeConfirmationSheetFragment();
                    gameState = GameState.IDLE;
                }
            });
        } else {
            gameState = GameState.PENDING_CONFIRMATION;
        }
    }

    public void updateTextInFragment(String newText) {
        TextView text = findViewById(R.id.fragment_text);
        if (text != null) {
            text.setText(newText);
        }
    }

    private void showConfirmationSheetFragment() {
        if (getSupportFragmentManager().findFragmentByTag(ConfirmationSheet.class.getSimpleName()) == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.setCustomAnimations(R.anim.slide_up, 0, 0, 0);
            transaction.add(R.id.fragment_container_view, ConfirmationSheet.class, null, ConfirmationSheet.class.getSimpleName());
            transaction.commit();
        }
    }

    private void closeConfirmationSheetFragment() {
        ConfirmationSheet confirmationSheetFragment = (ConfirmationSheet) getSupportFragmentManager().findFragmentByTag(ConfirmationSheet.class.getSimpleName());
        if (confirmationSheetFragment != null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.setCustomAnimations(0, R.anim.slide_down);
            transaction.remove(confirmationSheetFragment).commit();
        }
    }

    public void changeButtonState(Button button, GameButtonState state) {
        switch (state) {
            case INACTIVE:
                button.setBackgroundResource(R.drawable.game_button_inactive_style);
                button.setTextColor(getResources().getColor(R.color.light_gray));
                break;
            case IDLE:
                button.setBackgroundResource(R.drawable.game_button_idle_style);
                button.setTextColor(getResources().getColor(R.color.black));
                break;
            case CORRECT:
                button.setBackgroundResource(R.drawable.game_button_correct_style);
                button.setTextColor(getResources().getColor(R.color.black));
                break;
            case INCORRECT:
                button.setBackgroundResource(R.drawable.game_button_incorrect_style);
                button.setTextColor(getResources().getColor(R.color.black));
                break;
        }
    }
    private void shuffleQuestions() {
        if (questions != null && questions.length > 0) {
            shuffledQuestions = Arrays.asList(questions);
            Collections.shuffle(shuffledQuestions);
        }
    }

    private void showNextQuestion() {
        // Reset all buttons to their default state
        if(allButtons != null && allButtons.length > 0){
            for (Button button : allButtons) {
                changeButtonState(button, GameButtonState.INACTIVE);
            }
        }
        if (shuffledQuestions != null && !shuffledQuestions.isEmpty()) {
            if (currentQuestionIndex < shuffledQuestions.size()) {
                nextQuestion = shuffledQuestions.get(currentQuestionIndex);
                currentQuestionIndex++;

                // Set question text
                questionText.setText(nextQuestion.getQuestion());
                Log.e("Game", "Next question" + nextQuestion.toString());

                // Set answer options for each button
                allButtons[0].setText(nextQuestion.getOptions().getA().getText());
                allButtons[1].setText(nextQuestion.getOptions().getB().getText());
                allButtons[2].setText(nextQuestion.getOptions().getC().getText());
                allButtons[3].setText(nextQuestion.getOptions().getD().getText());


                // Update current question index
                currentQuestionIndexText.setText(String.valueOf(currentQuestionIndex));
            } else {
                // Restart the loop from the beginning
                currentQuestionIndex = 0;
                shuffleQuestions();
                // Recursive call to display the first question after shuffling
                showNextQuestion();
            }
        } else {
            Log.e("Game", "Questions not shuffled or empty");
        }
    }


    public void fetchQuestions() {
        db.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful()) {
                    DataSnapshot dataSnapshot = task.getResult();
                    if (dataSnapshot.exists()) {
                        GenericTypeIndicator<List<GameQuestion>> typeIndicator = new GenericTypeIndicator<List<GameQuestion>>() {};
                        List<GameQuestion> questionsList = dataSnapshot.getValue(typeIndicator);

                        if (questionsList != null) {
                            questions = questionsList.toArray(new GameQuestion[0]);
                            // Shuffle the questions
                            shuffleQuestions();
                            // Initial setup
                            if (questions.length > 0) {
                                totalNumOfQuestionsText.setText(String.valueOf(questions.length));
                                currentQuestionIndex = 0;
                                showNextQuestion();
                            }
                        } else {
                            Log.e("firebase", "Failed to parse questions list");
                        }
                    } else {
                        Log.e("firebase", "No data in the 'questions' node");
                    }
                } else {
                    Log.e("firebase", "Error getting data", task.getException());
                }
            }
        });
    }

    private String getSelectedAnswerText(Button selectedButton) {
        return selectedButton.getText().toString();
    }

    // Method to find the index of the correct button
    private int findCorrectButtonIndex() {
        if (nextQuestion != null && nextQuestion.getOptions() != null) {
            GameQuestion.Options options = nextQuestion.getOptions();
            if (options.getA().isCorrect()) return 0;
            if (options.getB().isCorrect()) return 1;
            if (options.getC().isCorrect()) return 2;
            if (options.getD().isCorrect()) return 3;
        }
        return -1; // Return -1 if no correct button found
    }

    private boolean checkAnswer(Button selectedButton) {
        // Identify which option (A, B, C, D) corresponds to the selectedButton
        int selectedOptionIndex = -1;
        for (int i = 0; i < allButtons.length; i++) {
            if (allButtons[i] == selectedButton) {
                selectedOptionIndex = i;
                break;
            }
        }
        // Check if the selected option is correct
        if (selectedOptionIndex != -1) {
            switch (selectedOptionIndex) {
                case 0:
                    return nextQuestion.getOptions().getA().isCorrect();
                case 1:
                    return nextQuestion.getOptions().getB().isCorrect();
                case 2:
                    return nextQuestion.getOptions().getC().isCorrect();
                case 3:
                    return nextQuestion.getOptions().getD().isCorrect();
            }
        }
        return false;
    }

    private void updateUserPoints(int points){
        if (loggedUser != null) {
            db = FirebaseDatabase.getInstance().getReference().child("users").child(loggedUser.getUid());
            db.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if(task.isSuccessful()){
                        try {
                            User currentUser = task.getResult().getValue(User.class);
                            if (currentUser != null) {
                                int currentPoints = Math.toIntExact(currentUser.points);
                                int newPoints = currentPoints + points;
                                db.child("points").setValue(newPoints);
                                gamePagePoints = findViewById(R.id.game_page_points);
                                gamePagePoints.setText(String.valueOf(newPoints));
                            }
                        }
                        catch (NullPointerException e){
                            Log.e("NoData", e.getMessage());
                        }
                    }
                }
            });
        }
    }
}
