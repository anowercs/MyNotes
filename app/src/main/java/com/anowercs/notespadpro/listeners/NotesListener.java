package com.anowercs.notespadpro.listeners;

import com.anowercs.notespadpro.entities.Note;

public interface NotesListener {
    void onNoteCLicked(Note note, int position);
}
