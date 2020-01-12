package chinmayd.notesapp.Adapters;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import chinmayd.notesapp.DataModels.Notes;
import chinmayd.notesapp.Database.DatabaseHelper;
import chinmayd.notesapp.R;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> implements Filterable {
    private static final String TAG = RecyclerViewAdapter.class.getSimpleName();

    private Context context;

    private ArrayList<Notes> notes;

    private ArrayList<Notes> notesFiltered;

    private Dialog updateDialog;

    private DatabaseHelper myDB;

    private showNoNoteView showNoNoteView;

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitle, txtData, txtTimeStamp;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            txtTitle = itemView.findViewById(R.id.noteTitle);
            txtData = itemView.findViewById(R.id.etNote);
            txtTimeStamp = itemView.findViewById(R.id.timeStamp);

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Vibrator vibe = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
                    vibe.vibrate(25);
                    update(getAdapterPosition());
                    return false;
                }
            });
        }
    }

    public RecyclerViewAdapter(Context context, ArrayList<Notes> notes, showNoNoteView showNoNoteView) {
        this.context = context;
        this.notes = notes;
        this.notesFiltered = notes;
        this.showNoNoteView = showNoNoteView;
    }

    @NonNull
    @Override
    public RecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.notes_view, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewAdapter.ViewHolder viewHolder, int i) {
        Notes notes = notesFiltered.get(i);

        if (notes != null) {
            if (viewHolder.txtTitle != null) {
                viewHolder.txtTitle.setText(notes.getTitle());
            }

            if (viewHolder.txtData != null) {
                viewHolder.txtData.setText(notes.getData());
            }

            if (viewHolder.txtTimeStamp != null) {
                viewHolder.txtTimeStamp.setText(formatDate(notes.getTimeStamp()));
            }
        }
    }

    @Override
    public int getItemCount() {
        return notesFiltered.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String charString = constraint.toString();

                if (charString.isEmpty()) {
                    notesFiltered = notes;
                } else {
                    ArrayList<Notes> filteredNotesList = new ArrayList<>();

                    for (Notes row : notes) {
                        if (row.getTitle().toLowerCase().contains(charString.toLowerCase()) || row.getData().toLowerCase().contains(charString.toLowerCase())) {
                            filteredNotesList.add(row);
                        }
                    }

                    notesFiltered = filteredNotesList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = notesFiltered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                notesFiltered = (ArrayList<Notes>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    public interface showNoNoteView {
        void showNoNote(Boolean visibility);
    }

    private String formatDate(String dateStr) {
        try {
            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = fmt.parse(dateStr);
            SimpleDateFormat fmtOut = new SimpleDateFormat("MMM d");
            return fmtOut.format(date);
        } catch (ParseException e) {
            Log.i(TAG, "formatDate ParseException: " + e);
        }

        return "";
    }

    private void update(final int position) {
        myDB = new DatabaseHelper(context);

        updateDialog = new Dialog(context);
        updateDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        updateDialog.setContentView(R.layout.update_layout);

        updateDialog.setCancelable(false);

        updateDialog.show();

        Button btnUpdate = updateDialog.findViewById(R.id.btnUpdate);
        Button btnCloseUpdate = updateDialog.findViewById(R.id.btnCloseUpdate);

        final EditText etTitle = updateDialog.findViewById(R.id.etTitle);
        final EditText etNote = updateDialog.findViewById(R.id.etNote);

        btnCloseUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Cursor cursor = myDB.getContents(notesFiltered.get(position).getId());

                if (cursor.moveToFirst()) {
                    do {

                        String noteTitle = cursor.getString(cursor.getColumnIndex("TITLE"));
                        String noteData = cursor.getString(cursor.getColumnIndex("DATA"));

                        if (noteTitle.equals(etTitle.getText().toString().trim()) && noteData.equals(etNote.getText().toString().trim())) {
                            updateDialog.dismiss();
                        } else {
                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            builder.setMessage("Close without updating?");
                            builder.setCancelable(false);

                            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    updateDialog.dismiss();
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
                    while (cursor.moveToNext());
                }
                cursor.close();
            }
        });

        // Get data of the corresponding ID from the database .
        Cursor cursor = myDB.getContents(notesFiltered.get(position).getId());
        Log.i(TAG, "CursorCount: " + cursor.getCount());

        // If cursor moveToFirst is true, then set data to the EditTexts
        if (cursor.moveToFirst()) {
            do {
                String title = cursor.getString(cursor.getColumnIndex("TITLE"));
                String note = cursor.getString(cursor.getColumnIndex("DATA"));

                etTitle.setText(title);
                etTitle.setSelection(etTitle.getText().length());
                etNote.setText(note);
                etNote.setSelection(etNote.getText().length());

                Log.i(TAG, "ViewData Title: " + title + " Note: " + note);
            }
            while (cursor.moveToNext());
        }
        cursor.close();

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check if the entered data is valid. If it's invalid display a AlertDialog. Else update the row corresponding to that ID.
                Cursor cursor = myDB.getContents(notesFiltered.get(position).getId());
                Log.i(TAG, "CursorCount: " + cursor.getCount());

                if (cursor.getCount() == 0) {
                    Toast.makeText(context, "ID doesn't exist!", Toast.LENGTH_SHORT).show();
                }

                if (cursor.moveToFirst()) {
                    do {

                        String title = cursor.getString(cursor.getColumnIndex("TITLE"));
                        String note = cursor.getString(cursor.getColumnIndex("DATA"));

                        // Checks if the data is modified or not.
                        if (title.equals(etTitle.getText().toString().trim()) && note.equals(etNote.getText().toString().trim())) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            builder.setMessage("You have not made any changes, cannot update.");
                            builder.setPositiveButton("OK", null);
                            AlertDialog dialog = builder.create();
                            dialog.show();
                            TextView textView = dialog.findViewById(android.R.id.message);
                            Typeface myCustomFont = ResourcesCompat.getFont(context, R.font.encodesans_regular);
                            textView.setTypeface(myCustomFont);
                        } else {
                            if (etTitle.length() == 0 && etNote.length() == 0) {
                                etTitle.setError("Enter a title!");
                                etNote.setError("Enter your note!");
                            } else if (etTitle.length() == 0) {
                                etTitle.setError("Enter a title!");
                            } else if (etNote.length() == 0) {
                                etNote.setError("Enter your note!");
                            } else {
                                Boolean update = myDB.updateData(notesFiltered.get(position).getId(), etTitle.getText().toString().trim(), etNote.getText().toString().trim());
                                int id = notesFiltered.get(position).getId();

                                if (update) {
                                    Notes notes1 = new Notes();
                                    notes1.setId(notesFiltered.get(position).getId());
                                    notes1.setData(etNote.getText().toString().trim());
                                    notes1.setTitle(etTitle.getText().toString().trim());
                                    notes1.setTimeStamp(notesFiltered.get(position).getTimeStamp());
                                    Toast.makeText(context, "Successfully updated the Note!", Toast.LENGTH_SHORT).show();
                                    notesFiltered.set(position, notes1);

                                    for (int i = 0; i < notes.size(); i++) {
                                        if (notes.get(i).getId() == id) {
                                            notes.set(i, notes1);
                                        }
                                    }

                                    notifyItemChanged(position);
                                    updateDialog.dismiss();

                                    if (notesFiltered.size() == 0 && notes.size() == 0) {
                                        showNoNoteView.showNoNote(true);
                                    }
                                }
                            }
                        }
                        Log.i(TAG, "ViewData Title: " + etTitle + " Note: " + etNote);
                    }
                    while (cursor.moveToNext());
                }
                cursor.close();
            }
        });
    }

    public void delete(final int delPos) {
        myDB = new DatabaseHelper(context);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Are you sure you want to delete this item?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Integer deleteRow = myDB.deleteData(notesFiltered.get(delPos).getId());
                Log.d(TAG, "onClick: deleteRow ID: " + notesFiltered.get(delPos).getId());
                int id = notesFiltered.get(delPos).getId();

                if (deleteRow > 0) {
                    notesFiltered.remove(delPos);
                    //Toast.makeText(context, "Successfully deleted the data!", Toast.LENGTH_SHORT).show();

                    for (int i = 0; i < notes.size(); i++) {
                        Log.d(TAG, "onClick: notesList: " + notes.get(i).getId());

                        if (notes.get(i).getId() == id) {
                            notes.remove(i);
                        }
                    }

                    notifyItemRemoved(delPos);

                    if (notesFiltered.size() == 0 && notes.size() == 0) {
                        showNoNoteView.showNoNote(true);
                    }
                }
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                notifyDataSetChanged();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
        TextView textView = dialog.findViewById(android.R.id.message);
        Typeface myCustomFont = ResourcesCompat.getFont(context, R.font.encodesans_regular);
        textView.setTypeface(myCustomFont);
    }
}