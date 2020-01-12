package chinmayd.notesapp.Activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;

import chinmayd.notesapp.Adapters.RecyclerViewAdapter;
import chinmayd.notesapp.DataModels.Notes;
import chinmayd.notesapp.Database.DatabaseHelper;
import chinmayd.notesapp.R;
import me.toptas.fancyshowcase.FancyShowCaseQueue;
import me.toptas.fancyshowcase.FancyShowCaseView;
import me.toptas.fancyshowcase.FocusShape;

public class MainActivity extends AppCompatActivity implements RecyclerViewAdapter.showNoNoteView {
    private static final String TAG = MainActivity.class.getSimpleName();

    private DatabaseHelper myDB;

    private ArrayList<Notes> notesArrayList;

    private RecyclerViewAdapter adapter;

    private Dialog addDialog;

    private Context context = MainActivity.this;

    private SearchView searchView;

    private TextView noNotes;

    private SharedPreferences prefs = null;

    private FloatingActionButton fab;

    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences("chinmayd.notesapp", MODE_PRIVATE);

        noNotes = findViewById(R.id.noNotes);

        myDB = new DatabaseHelper(this);

        notesArrayList = new ArrayList<>();
        getDataFromDB();
        recyclerView = findViewById(R.id.recyclerView);
        adapter = new RecyclerViewAdapter(this, notesArrayList, this);
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_NONE);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder viewHolder1) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
                adapter.delete(viewHolder.getAdapterPosition());
            }

        }).attachToRecyclerView(recyclerView);

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addDialog = new Dialog(MainActivity.this);
                addDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                addDialog.setContentView(R.layout.add_layout);
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
                addDialog.setCancelable(false);

                addDialog.show();

                Button btnAdd = addDialog.findViewById(R.id.btnAdd);
                Button btnCloseAdd = addDialog.findViewById(R.id.btnCloseAdd);

                final EditText etTitle = addDialog.findViewById(R.id.etTitle);
                final EditText etNote = addDialog.findViewById(R.id.etNote);

                btnCloseAdd.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String title = etTitle.getText().toString().trim();
                        String data = etNote.getText().toString().trim();

                        if (title.length() != 0 && data.length() != 0) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                            builder.setMessage("Discard Note?");
                            builder.setCancelable(false);

                            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    addDialog.dismiss();
                                }
                            });

                            builder.setNegativeButton("No", null);
                            AlertDialog dialog = builder.create();
                            dialog.show();
                            TextView textView = dialog.findViewById(android.R.id.message);
                            Typeface myCustomFont = ResourcesCompat.getFont(context, R.font.encodesans_regular);
                            textView.setTypeface(myCustomFont);
                        } else {
                            addDialog.dismiss();
                        }
                    }
                });

                btnAdd.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String title = etTitle.getText().toString().trim();
                        String data = etNote.getText().toString().trim();

                        if (etTitle.length() != 0 && etNote.length() != 0) {
                            long id = myDB.addData(title, data);
                            Notes n = myDB.getNote(id);

                            if (n != null) {
                                notesArrayList.add(0, n);
                            }

                            adapter.notifyDataSetChanged();
                            RecyclerShowcase();
                            addDialog.dismiss();
                        }

                        if (etTitle.length() == 0 && etNote.length() == 0) {
                            etTitle.setError("Enter a title!");
                            etNote.setError("Enter your note!");
                        } else if (etTitle.length() == 0) {
                            etTitle.setError("Enter a title!");
                        } else if (etNote.length() == 0) {
                            etNote.setError("Enter your note!");
                        }

                        if (notesArrayList.size() == 0) {
                            noNotes.setVisibility(View.VISIBLE);
                        } else {
                            noNotes.setVisibility(View.GONE);
                        }
                    }
                });
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (prefs.getBoolean("firstrun", true)) {
            new FancyShowCaseView.Builder(this)
                    .title("Click here to create a note")
                    .focusOn(fab)
                    .build()
                    .show();
        }
    }

    private void RecyclerShowcase() {
        if (notesArrayList.size() != 0 && prefs.getBoolean("firstrun", true)) {
            final FancyShowCaseView fancyShowCaseView1 = new FancyShowCaseView.Builder(this)
                    .title("Hold to Update or Swipe to Delete")
                    .titleSize(24, TypedValue.COMPLEX_UNIT_SP)
                    .focusOn(recyclerView)
                    .focusShape(FocusShape.ROUNDED_RECTANGLE)
                    .roundRectRadius(90)
                    .build();

            final FancyShowCaseView fancyShowCaseView2 = new FancyShowCaseView.Builder(this)
                    .title("Click here to search your notes")
                    .titleSize(24, TypedValue.COMPLEX_UNIT_SP)
                    .focusOn(searchView)
                    .focusCircleRadiusFactor(0.35)
                    .build();

            FancyShowCaseQueue mQueue = new FancyShowCaseQueue().add(fancyShowCaseView1).add(fancyShowCaseView2);

            mQueue.show();

            prefs.edit().putBoolean("firstrun", false).apply();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        TextView searchText = searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        Typeface myCustomFont = ResourcesCompat.getFont(context, R.font.encodesans_regular);
        searchText.setTypeface(myCustomFont);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setMaxWidth(Integer.MAX_VALUE);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                adapter.getFilter().filter(s);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                adapter.getFilter().filter(s);
                return false;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_search) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void getDataFromDB() {
        Log.i(TAG, "getDataFromDB: called");
        Cursor data = myDB.getListContents();
        int numRows = data.getCount();

        if (numRows == 0) {
            noNotes.setVisibility(View.VISIBLE);
            //Toast.makeText(this, "Database Empty!", Toast.LENGTH_SHORT).show();
        } else {
            noNotes.setVisibility(View.GONE);
            int i = 0;
            while (data.moveToNext()) {
                Notes notes = new Notes(data.getInt(0), data.getString(1), data.getString(2), data.getString(3));
                notesArrayList.add(i, notes);
                System.out.println(data.getString(0) + " " + data.getString(1) + " " + data.getString(2) + " " + data.getString(3));
                System.out.println(notesArrayList.get(i).getTitle());
                i++;
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (!searchView.isIconified()) {
            searchView.setIconified(true);
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Are you sure you want to quit?");
            builder.setCancelable(false);

            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    MainActivity.super.onBackPressed();
                }
            });

            builder.setNegativeButton("No", null);
            AlertDialog dialog = builder.create();
            dialog.show();
            TextView textView = dialog.findViewById(android.R.id.message);
            Typeface myCustomFont = ResourcesCompat.getFont(context, R.font.encodesans_regular);
            textView.setTypeface(myCustomFont);
        }
    }

    @Override
    public void showNoNote(Boolean visibility) {
        if (visibility) {
            noNotes.setVisibility(View.VISIBLE);
        } else {
            noNotes.setVisibility(View.GONE);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }
}
