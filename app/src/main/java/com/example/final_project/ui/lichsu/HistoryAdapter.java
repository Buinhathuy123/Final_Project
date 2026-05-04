package com.example.final_project.ui.lichsu;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.final_project.R;
import com.example.final_project.data.model.HistoryItem;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private List<HistoryItem> list;

    public HistoryAdapter(List<HistoryItem> list) {
        this.list = list;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtScore, txtLevel, txtDate;

        public ViewHolder(View view) {
            super(view);
            txtScore = view.findViewById(R.id.txtScore);
            txtLevel = view.findViewById(R.id.txtLevel);
            txtDate = view.findViewById(R.id.txtDate);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        HistoryItem item = list.get(position);

        holder.txtScore.setText(item.getScore() + " điểm");
        holder.txtLevel.setText(item.getLevel());
        holder.txtDate.setText(formatDate(item.getDate()));

        holder.itemView.setOnClickListener(v -> {
            Intent i = new Intent(v.getContext(), ChiTietLichSuActivity.class);

            i.putExtra("total_score", item.getScore());
            i.putExtra("level", item.getLevel());
            i.putExtra("date", item.getDate());
            i.putExtra("quiz_score", item.getQuizScore());
            i.putExtra("voice_res", item.getVoiceResult());
            i.putExtra("face_res", item.isFaceResult());

            v.getContext().startActivity(i);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    // 🔥 FORMAT DATE
    private String formatDate(String raw) {

        if (raw == null || raw.isEmpty()) return "--/--/----";

        try {
            Date date;

            if (raw.contains("T")) {
                SimpleDateFormat iso =
                        new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX", Locale.getDefault());
                date = iso.parse(raw);
            } else {
                SimpleDateFormat old =
                        new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH);
                date = old.parse(raw);
            }

            return new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date);

        } catch (Exception e) {
            return raw;
        }
    }
}