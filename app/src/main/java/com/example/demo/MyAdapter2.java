package com.example.demo;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.demo.Entities.Game;
import com.squareup.picasso.Picasso;

import java.util.List;

public class MyAdapter2 extends RecyclerView.Adapter<MyAdapter2.MyViewHolder> {

    //Variables
    List<Game> gameList;
    Context context;
    public MyAdapterListener onClickListener;

    //Interface for listeners
    public interface MyAdapterListener {
        void addRemoveButtonOnClick(View v,int position,List<Game> gameList);

        void addEditButtonOnClick(View v, int position,List<Game> gameList,TextView hoursPlayed);

        void addHourButtonOnClick(View v, int position,List<Game> gameList,TextView hoursPlayed);

        void addRemoveHourButtonOnClick(View v,int position,List<Game> gameList,TextView hourPlayed);

        void addCompletedButtonOnClick(View v, int position,List<Game> gameList);
    }

    //Constructor
    public MyAdapter2(Context ct, List<Game> games,MyAdapterListener listener){
        context = ct;
        gameList = games;
        onClickListener = listener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.library_row, parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

            //Setting text
            holder.libraryGameName.setText(gameList.get(position).getTitle());
            holder.libraryHours.setText(gameList.get(position).getHoursPlayed());

            //Setting image
            Picasso.get().load(gameList.get(position).getImageUrl()).into(holder.libraryGameImage);

            //Condition for completed game or not
            if(gameList.get(position).getCompleted().equals("Y")){
                holder.libraryProgress.setText(R.string.library_completed);
                holder.libraryEditButton.setEnabled(false);
                holder.libraryRemoveHour.setEnabled(false);
                holder.libraryAddHour.setEnabled(false);
                holder.libraryHours.setTextColor(Color.rgb(127,255,0));
                holder.libraryCompleteButton.setText(R.string.library_button_uncompleted);
            }else {
                holder.libraryProgress.setText(R.string.library_in_progress);
                holder.libraryEditButton.setEnabled(true);
                holder.libraryRemoveHour.setEnabled(true);
                holder.libraryAddHour.setEnabled(true);
                holder.libraryCompleteButton.setText(R.string.library_completed_button);
            }
    }

    @Override
    public int getItemCount() {
        return gameList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        //Variables
        TextView libraryGameName,libraryHours,libraryProgress;
        ImageView libraryGameImage;
        Button libraryEditButton,libraryRemoveHour,libraryAddHour,libraryCompleteButton,libraryRemoveButton;


        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            //Assigning views
            libraryGameName = itemView.findViewById(R.id.library_game_name);
            libraryProgress = itemView.findViewById(R.id.library_progress);
            libraryHours = itemView.findViewById(R.id.library_hours);
            libraryGameImage = itemView.findViewById(R.id.library_image);
            libraryEditButton = itemView.findViewById(R.id.library_edit_button);
            libraryRemoveHour = itemView.findViewById(R.id.library_remove_hour);
            libraryAddHour = itemView.findViewById(R.id.library_add_hour);
            libraryCompleteButton = itemView.findViewById(R.id.library_completed_button);
            libraryRemoveButton = itemView.findViewById(R.id.library_remove_button);

            //-----------------------------------Setting listeners----------------------------------
            libraryRemoveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("ButtonClicked", "Button Clicked in Adapted");
                    onClickListener.addRemoveButtonOnClick(v,getAdapterPosition(),gameList);
                }
            });

            libraryEditButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    onClickListener.addEditButtonOnClick(v,getAdapterPosition(),gameList,libraryHours);
                }
            });

            libraryAddHour.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickListener.addHourButtonOnClick(v,getAdapterPosition(),gameList,libraryHours);
                }
            });

            libraryRemoveHour.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickListener.addRemoveHourButtonOnClick(v,getAdapterPosition(),gameList,libraryHours);
                }
            });

            libraryCompleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(libraryCompleteButton.getText().toString().equals(context.getString(R.string.library_completed_button))){
                        libraryEditButton.setEnabled(false);
                        libraryAddHour.setEnabled(false);
                        libraryRemoveHour.setEnabled(false);
                        libraryHours.setTextColor(Color.rgb(127,255,0));
                        libraryCompleteButton.setText(context.getString(R.string.library_button_uncompleted));
                        libraryProgress.setText(context.getString(R.string.library_completed));
                    }else {
                        libraryEditButton.setEnabled(true);
                        libraryAddHour.setEnabled(true);
                        libraryRemoveHour.setEnabled(true);
                        libraryCompleteButton.setText(context.getString(R.string.library_completed_button));
                        libraryHours.setTextColor(context.getResources().getColor(R.color.colorAccent));
                        libraryProgress.setText(context.getString(R.string.library_in_progress));
                    }
                    onClickListener.addCompletedButtonOnClick(v,getAdapterPosition(),gameList);
                }
            });
        }
        //------------------------------------------------------------------------------------------
    }
}
