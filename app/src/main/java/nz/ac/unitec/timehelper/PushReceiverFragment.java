package nz.ac.unitec.timehelper;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 * Created by eugene on 28/08/2017.
 */

public class PushReceiverFragment extends DialogFragment {
    /**
     * PushReceiverFragment constructor.
     * @param title Title of the Alert, referenced by its resource id.
     * @param message Message contents of the Alert dialog.
     * @return The constructed AlertDialogFragment.
     */
    public static PushReceiverFragment newInstance(String title, String message) {
        PushReceiverFragment frag = new PushReceiverFragment();
        Bundle args = new Bundle();

        args.putString("title", title);
        args.putString("message", message);

        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String title = getArguments().getString("title");
        String message = getArguments().getString("message");

        return new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                            }
                        }
                ).create();
    }
}
