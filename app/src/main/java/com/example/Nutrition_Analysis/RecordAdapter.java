package com.example.Nutrition_Analysis;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.test_app.R;

import java.io.File;
import java.util.List;

public class RecordAdapter extends RecyclerView.Adapter<RecordAdapter.ViewHolder> {

    private final List<NutritionRecord> recordList;

    public RecordAdapter(List<NutritionRecord> recordList) {
        this.recordList = recordList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_record, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NutritionRecord record = recordList.get(position);  // This should no longer show an error

        // Set the nutrition details
        holder.nutritionDetailsTextView.setText(record.getNutritionDetails());

        // Set the image (if the path is valid)
        File imgFile = new File(record.getImagePath());
        if (imgFile.exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            holder.imageView.setImageBitmap(bitmap);
        } else {
            holder.imageView.setImageResource(android.R.drawable.ic_menu_report_image); // Placeholder image
        }
    }

    @Override
    public int getItemCount() {
        return recordList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView nutritionDetailsTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            nutritionDetailsTextView = itemView.findViewById(R.id.nutritionDetailsTextView);
        }
    }
}
