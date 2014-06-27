package com.globant.next2you.util;

public class Util {

	public final static boolean isValidEmail(CharSequence target) {
		if (target == null) {
			return false;
		}

		return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
	}

}
