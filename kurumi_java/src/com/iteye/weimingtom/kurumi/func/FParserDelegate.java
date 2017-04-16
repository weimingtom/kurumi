package com.iteye.weimingtom.kurumi.func;

import com.iteye.weimingtom.kurumi.core.LuaDo;
import com.iteye.weimingtom.kurumi.model.LuaStateObject;

public class FParserDelegate implements Pfunc {
	public FParserDelegate() {

	}

	public final void exec(LuaStateObject L, Object ud) {
		LuaDo.f_parser(L, ud);
	}
}