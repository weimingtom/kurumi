package com.iteye.weimingtom.kurumi.test;

import com.iteye.weimingtom.kurumi.core.LuaProgram;
import com.iteye.weimingtom.kurumi.core.LuacProgram;

public class Program {
	private static int main(String[] args) {
		if (false) {
			LuacProgram.MainLuac(args);
		}
		else {
			LuaProgram.MainLua(args);
		}
		return 0;
	}
}