package io.sharif.pavilion.serverSide.dialoges;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.support.v7.widget.LinearLayoutCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import io.sharif.pavilion.R;
import io.sharif.pavilion.utility.Statics;

/**
 * Created by EsiJOOn on 5/29/2016.
 */
public class SimpleDialog {
    AlertDialog dialog;
    public SimpleDialog(final Activity activity, final String dialogText){

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();
//                LinearLayout l= (LinearLayout) activity.findViewById(R.id.dialogue_layout);
        View mainView=inflater.inflate(R.layout.simple_dialogue, null);
        LinearLayoutCompat m= (LinearLayoutCompat) mainView.findViewById(R.id.dialogue_layout);
        m.setBackgroundColor(activity.getResources().getColor(Statics.getRandomDiologColor()));

        TextView t= (TextView) mainView.findViewById(R.id.simpletext);
        View.OnLongClickListener listener=new View.OnLongClickListener() {
           @Override
            public boolean onLongClick(View v) {
//                dialog.dismiss();
                ClipboardManager cm = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
                cm.setText(dialogText);
                Toast.makeText(activity, "کپی شد!", Toast.LENGTH_SHORT).show();
                return true;
            }

        };
        t.setOnLongClickListener(listener);
        m.setOnLongClickListener(listener);

        builder.setView(mainView);
        dialog=builder.create();

        TextView simpleText= (TextView) mainView.findViewById(R.id.simpletext);
        simpleText.setText(dialogText);
        dialog.show();

    }

    public void dismiss(){
        dialog.dismiss();
    }
    public void setCancelable(boolean b){
        dialog.setCancelable(b);
    }
}

