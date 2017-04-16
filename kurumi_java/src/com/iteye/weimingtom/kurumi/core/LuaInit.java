package com.iteye.weimingtom.kurumi.core;

import com.iteye.weimingtom.kurumi.func.LuaCFunction;
import com.iteye.weimingtom.kurumi.model.CharPtr;
import com.iteye.weimingtom.kurumi.model.LuaLReg;
import com.iteye.weimingtom.kurumi.model.LuaStateObject;

//
// ** $Id: linit.c,v 1.14.1.1 2007/12/27 13:02:25 roberto Exp $
// ** Initialization of libraries for lua.c
// ** See Copyright Notice in lua.h
// 
public class LuaInit {
	private final static LuaLReg[] lualibs = { new LuaLReg(CharPtr.toCharPtr(""), new LuaInit_delegate("LuaBaseLib.luaopen_base")), new LuaLReg(CharPtr.toCharPtr(LuaLib.LUA_LOADLIBNAME), new LuaInit_delegate("LuaLoadLib.luaopen_package")), new LuaLReg(CharPtr.toCharPtr(LuaLib.LUA_TABLIBNAME), new LuaInit_delegate("LuaTableLib.luaopen_table")), new LuaLReg(CharPtr.toCharPtr(LuaLib.LUA_IOLIBNAME), new LuaInit_delegate("LuaIOLib.luaopen_io")), new LuaLReg(CharPtr.toCharPtr(LuaLib.LUA_OSLIBNAME), new LuaInit_delegate("LuaOSLib.luaopen_os")), new LuaLReg(CharPtr.toCharPtr(LuaLib.LUA_STRLIBNAME), new LuaInit_delegate("LuaStrLib.luaopen_string")), new LuaLReg(CharPtr.toCharPtr(LuaLib.LUA_MATHLIBNAME), new LuaInit_delegate("LuaMathLib.luaopen_math")), new LuaLReg(CharPtr.toCharPtr(LuaLib.LUA_DBLIBNAME), new LuaInit_delegate("LuaDebugLib.luaopen_debug")), new LuaLReg(null, null) };

	public static class LuaInit_delegate implements LuaCFunction {
		private String name;

		public LuaInit_delegate(String name) {
			this.name = name;
		}

		public final int exec(LuaStateObject L) {
			if ((new String("LuaBaseLib.luaopen_base")).equals(name)) {
				return LuaBaseLib.luaopen_base(L);
			}
			else if ((new String("LuaLoadLib.luaopen_package")).equals(name)) {
				return LuaLoadLib.luaopen_package(L);
			}
			else if ((new String("LuaTableLib.luaopen_table")).equals(name)) {
				return LuaTableLib.luaopen_table(L);
			}
			else if ((new String("LuaIOLib.luaopen_io")).equals(name)) {
				return LuaIOLib.luaopen_io(L);
			}
			else if ((new String("LuaOSLib.luaopen_os")).equals(name)) {
				return LuaOSLib.luaopen_os(L);
			}
			else if ((new String("LuaStrLib.luaopen_string")).equals(name)) {
				return LuaStrLib.luaopen_string(L);
			}
			else if ((new String("LuaMathLib.luaopen_math")).equals(name)) {
				return LuaMathLib.luaopen_math(L);
			}
			else if ((new String("LuaDebugLib.luaopen_debug")).equals(name)) {
				return LuaDebugLib.luaopen_debug(L);
			}
			else {
				return 0;
			}
		}
	}


	public static void luaL_openlibs(LuaStateObject L) {
		for (int i = 0; i < lualibs.length - 1; i++) {
			LuaLReg lib = lualibs[i];
			Lua.lua_pushcfunction(L, lib.func);
			LuaAPI.lua_pushstring(L, lib.name);
			LuaAPI.lua_call(L, 1, 0);
		}
	}
}