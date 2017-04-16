package com.iteye.weimingtom.kurumi.func;

import com.iteye.weimingtom.kurumi.core.LuaDo;
import com.iteye.weimingtom.kurumi.model.LuaStateObject;

public class ResumeDelegate implements Pfunc {
	public final void exec(LuaStateObject L, Object ud) {
		LuaDo.resume(L, ud);
	}
}