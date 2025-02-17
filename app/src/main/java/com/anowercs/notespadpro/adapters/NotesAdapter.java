package com.anowercs.notespadpro.adapters;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.anowercs.notespadpro.R;
import com.anowercs.notespadpro.entities.Note;
import com.anowercs.notespadpro.listeners.NotesListener;
import com.bumptech.glide.Glide;
import com.makeramen.roundedimageview.RoundedImageView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NoteViewHolder> {

    private List<Note> notes;
    private final NotesListener notesListener;
    private Timer timer;
    private List<Note> notesSource;


    public NotesAdapter(List<Note> notes, NotesListener notesListener) {
        this.notes = notes;
        this.notesListener = notesListener;
        this.notesSource = new ArrayList<>(notes);


    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new NoteViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(R.layout.item_container_note, parent, false)
        );
    }



    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        try {
            // Prevent IndexOutOfBounds crashes
            if (position >= notes.size()) {
                return;
            }

            // Get note first to prevent multiple list access
            Note note = notes.get(position);
            if (note == null) {
                return;
            }

            // Fix for StaggeredGridLayoutManager
            StaggeredGridLayoutManager.LayoutParams layoutParams =
                    (StaggeredGridLayoutManager.LayoutParams) holder.itemView.getLayoutParams();
            layoutParams.setFullSpan(false);
            holder.itemView.setLayoutParams(layoutParams);

            // Set note data
            holder.setNote(note);

            // Set click listener directly - remove the tag check
            holder.layoutNote.setOnClickListener(v -> {
                int currentPosition = holder.getAdapterPosition();
                if (currentPosition != RecyclerView.NO_POSITION && notesListener != null) {
                    notesListener.onNoteCLicked(notes.get(currentPosition), currentPosition);
                }
            });

        } catch (Exception e) {
            Log.e("NotesAdapter", "Error binding view: " + e.getMessage());
        }
    }

    // Add onViewRecycled to clean up
    @Override
    public void onViewRecycled(@NonNull NoteViewHolder holder) {
        super.onViewRecycled(holder);
        // Clear click listener
        holder.layoutNote.setOnClickListener(null);
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder {

        TextView textTitle, textSubtitle, textDateTime;
        LinearLayout layoutNote;
        RoundedImageView imageNote;

        NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitle = itemView.findViewById(R.id.textTitle);
            textSubtitle = itemView.findViewById(R.id.textSubtitle);
            textDateTime = itemView.findViewById(R.id.textDateTime);
            layoutNote = itemView.findViewById(R.id.layoutNote);
            imageNote = itemView.findViewById(R.id.imageNote);
        }

        void setNote(Note note) {
            textTitle.setText(note.getTitle());
            if (note.getSubtitle().trim().isEmpty()) {
                textSubtitle.setVisibility(View.GONE);
            } else {
                textSubtitle.setVisibility(View.VISIBLE);
                textSubtitle.setText(note.getSubtitle());
            }

            textDateTime.setText(note.getDateTime());
            GradientDrawable gradientDrawable = (GradientDrawable) layoutNote.getBackground();
            if (note.getColor() != null) {
                gradientDrawable.setColor(Color.parseColor(note.getColor()));
            } else {
                gradientDrawable.setColor(Color.parseColor("#333333"));
            }

            // Handle S3 image loading
            // In your NoteViewHolder's setNote method
            if (note.getImagePath() != null && !note.getImagePath().trim().isEmpty()) {
                if (note.getImagePath().startsWith("http")) {
                    // Load from S3 URL
                    Glide.with(imageNote.getContext())
                            .load(note.getImagePath())
                            .placeholder(R.drawable.demo_profile)
                            .error(R.drawable.demo_profile_3)
                            .into(imageNote);
                    imageNote.setVisibility(View.VISIBLE);
                } else {
                    // Load from local path
                    Glide.with(imageNote.getContext())
                            .load(new File(note.getImagePath()))
                            .placeholder(R.drawable.demo_profile)
                            .error(R.drawable.demo_profile_3)
                            .into(imageNote);
                    imageNote.setVisibility(View.VISIBLE);
                }
            } else {
                imageNote.setVisibility(View.GONE);
            }
        }
    }

   public void searchNotes(final String searchKeyword) {
       // Cancel any existing timer to prevent multiple searches
       if (timer != null) {
           timer.cancel();
       }

       // Ensure notesSource is initialized
       if (notesSource == null) {
           notesSource = new ArrayList<>(notes);
       }

       // If search is empty or null, restore full list immediately
       if (searchKeyword == null || searchKeyword.trim().isEmpty()) {
           notes.clear();
           notes.addAll(notesSource);
           notifyDataSetChanged();
           return;
       }

       timer = new Timer();
       timer.schedule(new TimerTask() {
           @Override
           public void run() {
               try {
                   // Convert search keyword to lowercase for case-insensitive search
                   String query = searchKeyword.trim().toLowerCase();

                   // Create temporary list for filtered notes
                   List<Note> filteredNotes = new ArrayList<>();

                   // Search through all notes
                   for (Note note : notesSource) {
                       if (note != null && (
                               (note.getTitle() != null && note.getTitle().toLowerCase().contains(query)) ||
                                       (note.getSubtitle() != null && note.getSubtitle().toLowerCase().contains(query)) ||
                                       (note.getNoteText() != null && note.getNoteText().toLowerCase().contains(query))
                       )) {
                           filteredNotes.add(note);
                       }
                   }

                   // Update UI on main thread
                   new Handler(Looper.getMainLooper()).post(() -> {
                       notes.clear();
                       notes.addAll(filteredNotes);
                       notifyDataSetChanged();
                   });
               } catch (Exception e) {
                   Log.e("NotesAdapter", "Error during search: " + e.getMessage());
               }
           }
       }, 300); // Add a small delay to prevent too frequent updates
   }

    // Add this method to properly handle the timer cleanup
    public void cancelTimer() {
        if (timer != null) {
            timer.cancel();
        }
    }

    // Add this method to update the source data when new notes are loaded
    public void updateSourceData(List<Note> newNotes) {
        if (newNotes != null) {
            notesSource = new ArrayList<>(newNotes);
            notes = new ArrayList<>(newNotes);
            notifyDataSetChanged();
        }
    }


    public void refreshSourceList() {
        notes.clear();
        notes.addAll(notesSource);
        notifyDataSetChanged();
    }



    // Method to update the notes list with new data from DynamoDB
    public void updateNotes(List<Note> newNotes) {
        this.notes = newNotes;
        this.notesSource = newNotes;
        notifyDataSetChanged();
    }

}