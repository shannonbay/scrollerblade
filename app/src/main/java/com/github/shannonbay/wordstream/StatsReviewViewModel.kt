package com.github.shannonbay.wordstream

import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.style.BackgroundColorSpan
import android.text.style.ClickableSpan
import android.view.View
import androidx.lifecycle.ViewModel

class StatsReviewViewModel : ViewModel() {
    fun book() : Spannable {

        // Example text
        val longText = "This is a long paragraph. This is another sentence. And another."

        // Create a SpannableString
        val spannableString = SpannableString(longText);

        // Apply different background colors to specific phrases
        spannableString.setSpan(BackgroundColorSpan(Color.YELLOW), 10, 25, 0);
        spannableString.setSpan(BackgroundColorSpan(Color.GREEN), 30, 45, 0);

        // Make each phrase a clickable hyperlink
        val clickableSpan1 = object: ClickableSpan() {
            override fun onClick(view: View) : Unit {
                // Handle click for the first phrase
                showDialog("Clicked on the first phrase");
            }
        };

        val clickableSpan2 = object: ClickableSpan() {
            override fun onClick(view: View) {
                // Handle click for the second phrase
                showDialog("Clicked on the second phrase");
            }
        };

        spannableString.setSpan(clickableSpan1, 10, 25, 0);
        spannableString.setSpan(clickableSpan2, 30, 45, 0);

        // Apply the SpannableString to the TextView
//        textView.setText(spannableString);

        // Make the TextView clickable
 //       textView.setMovementMethod(android.text.method.LinkMovementMethod.getInstance());

        return SpannableString("asdf")
    }

    fun showDialog(message: String) {
        // Implement your dialog view here
       // Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}