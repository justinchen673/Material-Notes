package com.justin.justin.materialnotes;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends Activity implements View.OnClickListener {

    private InputMethodManager imm;
    private static final String TRANSLATION_X = "translationX";
    private static final String TRANSLATION_Y = "translationY";
    private View fabAction1;
    private float offset1;
    private int xTouch, yTouch, itemPosition;
    private boolean expanded = false;
    private boolean newEdit = true;
    private ImageButton fab, deleteFab;
    private EditText viewTitle, viewNote, editText, title;
    private ListView list;
    private TextView noNotesTv;
    SharedPrefs prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = new SharedPrefs();
        fab = findViewById(R.id.fab);
        fab.setOnClickListener(this);
        fabAction1 = findViewById(R.id.fab_action_1);
        fabAction1.setOnClickListener(this);
        deleteFab = findViewById(R.id.deleteFab);
        deleteFab.setOnClickListener(this);
        noNotesTv = findViewById(R.id.noNotesTv);
        list = findViewById(R.id.list);
        editText = findViewById(R.id.noteEdit);
        title = findViewById(R.id.title);
        viewTitle = findViewById(R.id.viewTitle2);
        viewNote = findViewById(R.id.viewNote2);

        final ViewGroup fabContainer = findViewById(R.id.fab_container);
        fabContainer.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                fabContainer.getViewTreeObserver().removeOnPreDrawListener(this);
                offset1 = (fab.getY() - fabAction1.getY()) * 7;
                return true;
            }
        });

        Set<String> titlesSet = prefs.getSet(getApplicationContext(), SharedPrefs.SharedKeys.titles);
        String[] titlesArray = titlesSet.toArray(new String[titlesSet.size()]);
        if (titlesArray.length == 0) {
            noNotesTv.setVisibility(View.VISIBLE);
        } else {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_activated_1, titlesArray);
            list.setAdapter(adapter);
            list.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View view, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        xTouch = (int) event.getX();
                        yTouch = (int) event.getY();
                    }
                    return false;
                }
            });
            listSetOnClickListener();
        }
    }

    @Override
    public void onClick(View v) {
        final View myView = findViewById(R.id.editlayout);
        final View otherView = findViewById(R.id.viewLayout);
        int cx = myView.getLeft() + myView.getRight();
        int cy = myView.getTop() + myView.getBottom();
        int finalRadius = myView.getWidth() + myView.getHeight();
        if (v.getId() == fab.getId()) {
            expanded = !expanded;
            if (expanded) {
                Animator anim = ViewAnimationUtils.createCircularReveal(myView, cx, cy, 0, finalRadius);
                myView.setVisibility(View.VISIBLE);
                anim.start();
                expandFab();
            } else {
                hideAnimation(myView, cx, cy, finalRadius);
                collapseFab();
                editText.setText("");
                title.setText("");
            }
            newEdit = true;
        } else if (v.getId() == fabAction1.getId()) {
            String wroteText;
            String titleText;
            if (newEdit) {
                wroteText = editText.getText().toString();
                titleText = title.getText().toString();
                doneButtonPress(wroteText, titleText, myView, cx, cy, finalRadius, newEdit);
            } else {
                wroteText = viewNote.getText().toString();
                titleText = viewTitle.getText().toString();
                doneButtonPress(wroteText, titleText, otherView, cx, cy, finalRadius, newEdit);
                AnimatorSet animatorSet = new AnimatorSet();
                animatorSet.playTogether(createExpandYAnimator(fab, offset1));
                animatorSet.start();
                animateFab();
                expanded = !expanded;
            }
        } else if (v.getId() == deleteFab.getId()) {
            Set<String> oldTitleSet = prefs.getSet(getApplicationContext(), SharedPrefs.SharedKeys.titles);
            String[] midTitleArray = oldTitleSet.toArray(new String[oldTitleSet.size()]);
            midTitleArray[itemPosition] = "";
            String[] newTitleArray = new String[midTitleArray.length - 1];
            int j = 0;
            for (int i = 0;  i < midTitleArray.length; i++) {
                if (i == itemPosition) {
                    if (itemPosition == midTitleArray.length - 1) {
                        break;
                    } else {
                        i++;
                    }
                }
                newTitleArray[j] = midTitleArray[i];
                j++;
            }
            Set<String> newTitleSet = new HashSet<>(Arrays.asList(newTitleArray));
            prefs.storeSet(getApplicationContext(), SharedPrefs.SharedKeys.titles, newTitleSet);

            Set<String> oldNoteSet = prefs.getSet(getApplicationContext(), SharedPrefs.SharedKeys.notes);
            String[] midNoteArray = oldNoteSet.toArray(new String[oldNoteSet.size()]);
            midNoteArray[itemPosition] = "";
            String[] newNoteArray = new String[midNoteArray.length - 1];
            j = 0;
            for (int i = 0;  i < midNoteArray.length; i++) {
                if (i == itemPosition) {
                    if (itemPosition == midNoteArray.length - 1) {
                        break;
                    } else {
                        i++;
                    }
                }
                newNoteArray[j] = midNoteArray[i];
                j++;
            }
            Set<String> newNoteSet = new HashSet<>(Arrays.asList(newNoteArray));
            prefs.storeSet(getApplicationContext(), SharedPrefs.SharedKeys.notes, newNoteSet);
            hideAnimation(otherView, cx, cy, finalRadius);
            collapseFab();
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(createExpandYAnimator(fab, offset1));
            animatorSet.start();
            animateFab();
            if (newTitleArray.length == 0) {
                noNotesTv.setVisibility(View.VISIBLE);
            } else {
                list.setVisibility(View.VISIBLE);
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_activated_1, newTitleArray);
                list.setAdapter(adapter);
            }
        }
    }

    private void doneButtonPress(String wroteText, String titleText, View myView, int cx, int cy, int finalRadius, boolean newEdit2)  {
        boolean okay = false;
        for (int i = 0; i < wroteText.length(); i++) {
            if (wroteText.charAt(i) != ' ') {
                okay = true;
            }
        }
        if (okay) {
            boolean titleFull = false;
            if (titleText.length() != 0) {
                for (int i = 0; i < wroteText.length(); i++) {
                    if (wroteText.charAt(i) != ' ') {
                        titleFull = true;
                    }
                }
            }
            Set<String> oldTitleSet = prefs.getSet(getApplicationContext(), SharedPrefs.SharedKeys.titles);
            String[] newTitleArray;
            if (newEdit2) {
                newTitleArray = oldTitleSet.toArray(new String[oldTitleSet.size() + 1]);
                for (int i = newTitleArray.length - 1; i > 0; i--) {
                    newTitleArray[i] = newTitleArray[i - 1];
                }
                if (titleFull) {
                    newTitleArray[0] = titleText;
                } else {
                    int firstLetter = -1;
                    int lastLetter = -1;
                    for (int i = 0; i < wroteText.length(); i++) {
                        if (firstLetter == -1 && wroteText.charAt(i) != ' ') {
                            firstLetter = i;
                        }
                        if (firstLetter != -1 && wroteText.charAt(i) == ' ') {
                            lastLetter = i;
                        }
                        if (lastLetter != -1 && firstLetter != -1) {
                            break;
                        }
                    }
                    if (lastLetter == -1) {
                        lastLetter = wroteText.length();
                    }
                    String finalTitle = wroteText.substring(firstLetter, lastLetter);
                    newTitleArray[0] = finalTitle;
                }
            } else {
                newTitleArray = oldTitleSet.toArray(new String[oldTitleSet.size()]);
                if (titleFull) {
                    newTitleArray[itemPosition] = titleText;
                } else {
                    int firstLetter = -1;
                    int lastLetter = -1;
                    for (int i = 0; i < wroteText.length(); i++) {
                        if (firstLetter == -1 && wroteText.charAt(i) != ' ') {
                            firstLetter = i;
                        }
                        if (firstLetter != -1 && wroteText.charAt(i) == ' ') {
                            lastLetter = i;
                        }
                        if (lastLetter != -1 && firstLetter != -1) {
                            break;
                        }
                    }
                    if (lastLetter == -1) {
                        lastLetter = wroteText.length();
                    }
                    String finalTitle = wroteText.substring(firstLetter, lastLetter);
                    newTitleArray[itemPosition] = finalTitle;
                }
            }
            Set<String> newTitleSet = new HashSet<>(Arrays.asList(newTitleArray));
            prefs.storeSet(getApplicationContext(), SharedPrefs.SharedKeys.titles, newTitleSet);

            imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
            imm.hideSoftInputFromWindow(title.getWindowToken(), 0);

            Set<String> oldSet = prefs.getSet(getApplicationContext(), SharedPrefs.SharedKeys.notes);
            String[] newArray;
            if (newEdit2) {
                newArray = oldSet.toArray(new String[oldSet.size() + 1]);
                for (int i = newArray.length - 1; i > 0; i--) {
                    newArray[i] = newArray[i - 1];
                }
                newArray[0] = wroteText;
            } else {
                newArray = oldSet.toArray(new String[oldSet.size()]);
                newArray[itemPosition] = wroteText;
            }

            Set<String> newSet = new HashSet<>(Arrays.asList(newArray));
            prefs.storeSet(getApplicationContext(), "notes", newSet);
            hideAnimation(myView, cx, cy, finalRadius);
            collapseFab();
            if (newTitleArray.length == 0) {
                noNotesTv.setVisibility(View.VISIBLE);
            } else {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_activated_1, newTitleArray);
                list.setAdapter(adapter);
                list.setVisibility(View.VISIBLE);
            }
            expanded = !expanded;
            editText.setText("");
            title.setText("");
            noNotesTv.setVisibility(View.INVISIBLE);
            listSetOnClickListener();

            Set<String> debugSet = prefs.getSet(getApplicationContext(), SharedPrefs.SharedKeys.notes);
            String[] debugArray = debugSet.toArray(new String[debugSet.size()]);
            for (int i = 0; i < debugArray.length; i++) {
                System.out.println("At " + i + " the array value is " + debugArray[i]);
            }
        } else {
            Toast.makeText(getApplicationContext(), "Empty notes can't be saved.", Toast.LENGTH_LONG).show();
        }
    }

    private void hideAnimation(final View myView, int cx, int cy, int finalRadius) {
        Animator anim = ViewAnimationUtils.createCircularReveal(myView, cx, cy, finalRadius, 0);
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                myView.setVisibility(View.INVISIBLE);
            }
        });
        anim.start();
    }

    private void expandFab() {
        fab.setImageResource(R.drawable.animated_plus);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(createExpandAnimator(fabAction1, offset1));
        animatorSet.start();
        animateFab();
    }

    private void collapseFab() {
        fab.setImageResource(R.drawable.animated_minus);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(createCollapseAnimator(fabAction1, offset1));
        animatorSet.start();
        animateFab();
    }

    private void animateFab() {
        Drawable drawable = fab.getDrawable();
        if (drawable instanceof Animatable) {
            ((Animatable) drawable).start();
        }
    }

    private Animator createExpandAnimator(View view, float offset) {
        return ObjectAnimator.ofFloat(view, TRANSLATION_X, 0, offset).setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime));
    }

    private Animator createCollapseAnimator(View view, float offset) {
        return ObjectAnimator.ofFloat(view, TRANSLATION_X, offset, 0).setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime));
    }

    private Animator createCollapseYAnimator(View view, float offset) {
        return ObjectAnimator.ofFloat(view, TRANSLATION_Y, 0, (offset * -2)).setDuration(getResources().getInteger(android.R.integer.config_longAnimTime));
    }

    private Animator createExpandYAnimator(View view, float offset) {
        return ObjectAnimator.ofFloat(view, TRANSLATION_Y, (offset * -2), 0).setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime));
    }
    
    private void listSetOnClickListener() {
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                itemPosition = position;
                final View myView = findViewById(R.id.viewLayout);
                Animator anim = ViewAnimationUtils.createCircularReveal(myView, xTouch, yTouch, 0, myView.getWidth() + myView.getHeight());
                anim.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        list.setVisibility(View.INVISIBLE);
                    }
                });
                myView.setVisibility(View.VISIBLE);
                anim.start();
                Set<String> titleSet = prefs.getSet(getApplicationContext(), SharedPrefs.SharedKeys.titles);
                String[] titleArray = titleSet.toArray(new String[titleSet.size()]);
                viewTitle.setText(titleArray[position]);
                Set<String> noteSet = prefs.getSet(getApplicationContext(), SharedPrefs.SharedKeys.notes);
                String[] noteArray = noteSet.toArray(new String[noteSet.size()]);
                viewNote.setText(noteArray[position]);
                AnimatorSet animatorSet = new AnimatorSet();
                animatorSet.playTogether(createCollapseYAnimator(fab, offset1));
                animatorSet.start();
                expandFab();
                newEdit = false;
            }
        });
    }

    @Override
    public void onBackPressed() {
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
        imm.hideSoftInputFromWindow(title.getWindowToken(), 0);
        imm.hideSoftInputFromWindow(viewNote.getWindowToken(), 0);
        imm.hideSoftInputFromWindow(viewTitle.getWindowToken(), 0);
        final View viewLayout = findViewById(R.id.viewLayout);
        final View editLayout = findViewById(R.id.editlayout);
        int viewVisibility = viewLayout.getVisibility();
        int editVisibility = editLayout.getVisibility();
        int tempX = viewLayout.getLeft() + viewLayout.getRight();
        int tempY = viewLayout.getTop() + viewLayout.getBottom();
        int radius = viewLayout.getWidth() + viewLayout.getHeight();
        if (viewVisibility == View.VISIBLE) {
            hideAnimation(viewLayout, tempX, tempY, radius);
            editLayout.setVisibility(View.INVISIBLE);
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(createExpandYAnimator(fab, offset1));
            animatorSet.start();
            animateFab();
            collapseFab();
            list.setVisibility(View.VISIBLE);
        }  else if (editVisibility == View.VISIBLE) {
            hideAnimation(editLayout, tempX, tempY, radius);
            collapseFab();
            expanded = !expanded;
            viewLayout.setVisibility(View.INVISIBLE);
            list.setVisibility(View.VISIBLE);
        }
        editText.setText("");
        title.setText("");
    }
}
