package com.example.demo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

    //Variables
    public String[] names,imageUrls;
    public Context context;
    public MyAdapterListener onClickListener;

    //Interface for onClick Listeners
    public interface MyAdapterListener {
        void addButtonOnClick(View v,int position, String[] names,String[] imageUrls);
    }

    //Constructor
    public MyAdapter(Context ct, String[] inputNames, String[] InputImageUrls,MyAdapterListener listener) {
        context = ct;
        names = inputNames;
        imageUrls = InputImageUrls;
        onClickListener = listener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.my_row, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.myText1.setText(names[position]);
        Picasso.get().load(imageUrls[position]).into(holder.myImage);
    }

    @Override
    public int getItemCount() {
        return imageUrls.length;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        Button addButton;
        TextView myText1;
        ImageView myImage;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            myText1 = itemView.findViewById(R.id.search_game_title);
            myImage = itemView.findViewById(R.id.search_game_image);
            addButton = (Button) itemView.findViewById(R.id.search_add_button);

            //On Click Listener
            addButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickListener.addButtonOnClick(v,getAdapterPosition(),names,imageUrls);
                }
            });
        }


    }
}



