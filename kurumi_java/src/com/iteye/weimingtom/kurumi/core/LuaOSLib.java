package com.iteye.weimingtom.kurumi.core;

import com.iteye.weimingtom.kurumi.func.LuaCFunction;
import com.iteye.weimingtom.kurumi.model.CharPtr;
import com.iteye.weimingtom.kurumi.model.LuaLReg;
import com.iteye.weimingtom.kurumi.model.LuaStateObject;
import com.iteye.weimingtom.kurumi.port.ClassType;
import com.iteye.weimingtom.kurumi.port.DateTimeProxy;
import com.iteye.weimingtom.kurumi.port.StreamProxy;

//
// ** $Id: loslib.c,v 1.19.1.3 2008/01/18 16:38:18 roberto Exp $
// ** Standard Operating System library
// ** See Copyright Notice in lua.h
// 

//using TValue = Lua.TValue;
//using StkId = TValue;
//using lua_Integer = System.Int32;
//using lua_Number = System.Double;

public class LuaOSLib {
	private static int os_pushresult(LuaStateObject L, int i, CharPtr filename) {
		int en = LuaConf.errno(); // calls to Lua API may change this value 
		if (i != 0) {
			LuaAPI.lua_pushboolean(L, 1);
			return 1;
		}
		else {
			LuaAPI.lua_pushnil(L);
			LuaAPI.lua_pushfstring(L, CharPtr.toCharPtr("%s: %s"), filename, LuaConf.strerror(en));
			LuaAPI.lua_pushinteger(L, en);
			return 3;
		}
	}

	private static int os_execute(LuaStateObject L) {
		CharPtr strCmdLine = CharPtr.toCharPtr("" + LuaAuxLib.luaL_optstring(L, 1, null));
		LuaAPI.lua_pushinteger(L, ClassType.processExec(strCmdLine.toString()));
		return 1;
	}

	private static int os_remove(LuaStateObject L) {
		CharPtr filename = LuaAuxLib.luaL_checkstring(L, 1);
		int result = 1;
		try {
			StreamProxy.Delete(filename.toString());
		}
		catch (java.lang.Exception e) {
			result = 0;
		}
		return os_pushresult(L, result, filename);
	}

	private static int os_rename(LuaStateObject L) {
		CharPtr fromname = LuaAuxLib.luaL_checkstring(L, 1);
		CharPtr toname = LuaAuxLib.luaL_checkstring(L, 2);
		int result;
		try {
			StreamProxy.Move(fromname.toString(), toname.toString());
			result = 0;
		}
		catch (java.lang.Exception e) {
			result = 1; // todo: this should be a proper error code
		}
		return os_pushresult(L, result, fromname);
	}

	private static int os_tmpname(LuaStateObject L) {
		LuaAPI.lua_pushstring(L, CharPtr.toCharPtr(StreamProxy.GetTempFileName()));
		return 1;
	}


	private static int os_getenv(LuaStateObject L) {
		LuaAPI.lua_pushstring(L, LuaConf.getenv(LuaAuxLib.luaL_checkstring(L, 1))); // if null push nil 
		return 1;
	}

	private static int os_clock(LuaStateObject L) {
		LuaAPI.lua_pushnumber(L, DateTimeProxy.getClock());
		return 1;
	}

//        
//		 ** {======================================================
//		 ** Time/Date operations
//		 ** { year=%Y, month=%m, day=%d, hour=%H, min=%M, sec=%S,
//		 **   wday=%w+1, yday=%j, isdst=? }
//		 ** =======================================================
//		 
	private static void setfield(LuaStateObject L, CharPtr key, int value) {
		LuaAPI.lua_pushinteger(L, value);
		LuaAPI.lua_setfield(L, -2, key);
	}

	private static void setboolfield(LuaStateObject L, CharPtr key, int value) {
		if (value < 0) { // undefined? 
			return; // does not set field 
		}
		LuaAPI.lua_pushboolean(L, value);
		LuaAPI.lua_setfield(L, -2, key);
	}

	private static int getboolfield(LuaStateObject L, CharPtr key) {
		int res;
		LuaAPI.lua_getfield(L, -1, key);
		res = Lua.lua_isnil(L, -1) ? -1 : LuaAPI.lua_toboolean(L, -1);
		Lua.lua_pop(L, 1);
		return res;
	}

	private static int getfield(LuaStateObject L, CharPtr key, int d) {
		int res;
		LuaAPI.lua_getfield(L, -1, key);
		if (LuaAPI.lua_isnumber(L, -1) != 0) {
			res = (int)LuaAPI.lua_tointeger(L, -1);
		}
		else {
			if (d < 0) {
				return LuaAuxLib.luaL_error(L, CharPtr.toCharPtr("field " + LuaConf.getLUA_QS() + " missing in date table"), key);
			}
			res = d;
		}
		Lua.lua_pop(L, 1);
		return res;
	}

	private static int os_date(LuaStateObject L) {
		CharPtr s = LuaAuxLib.luaL_optstring(L, 1, CharPtr.toCharPtr("%c"));
		DateTimeProxy stm = new DateTimeProxy();
		if (s.get(0) == '!') {
			// UTC? 
			stm.setUTCNow();
			s.inc(); // skip `!' 
		}
		else {
			stm.setNow();
		}
		if (LuaConf.strcmp(s, CharPtr.toCharPtr("*t")) == 0) {
			LuaAPI.lua_createtable(L, 0, 9); // 9 = number of fields 
			setfield(L, CharPtr.toCharPtr("sec"), stm.getSecond());
			setfield(L, CharPtr.toCharPtr("min"), stm.getMinute());
			setfield(L, CharPtr.toCharPtr("hour"), stm.getHour());
			setfield(L, CharPtr.toCharPtr("day"), stm.getDay());
			setfield(L, CharPtr.toCharPtr("month"), stm.getMonth());
			setfield(L, CharPtr.toCharPtr("year"), stm.getYear());
			setfield(L, CharPtr.toCharPtr("wday"), (int)stm.getDayOfWeek());
			setfield(L, CharPtr.toCharPtr("yday"), stm.getDayOfYear());
			setboolfield(L, CharPtr.toCharPtr("isdst"), stm.IsDaylightSavingTime() ? 1 : 0);
		}
		else {
			LuaAuxLib.luaL_error(L, CharPtr.toCharPtr("strftime not implemented yet")); // todo: implement this - mjf
			///#if false
//				CharPtr cc = new char[3];
//				luaL_Buffer b;
//				cc[0] = '%'; 
//				cc[2] = '\0';
//				luaL_buffinit(L, b);
//				for (; s[0] != 0; s.inc()) 
//				{
//					if (s[0] != '%' || s[1] == '\0')  /* no conversion specifier? */
//					{
//						luaL_addchar(b, s[0]);
//					}
//					else 
//					{
//						uint reslen;
//						CharPtr buff = new char[200];  /* should be big enough for any conversion result */
//						s.inc();
//						cc[1] = s[0];
//						reslen = strftime(buff, buff.Length, cc, stm);
//						luaL_addlstring(b, buff, reslen);
//					}
//				}
//				luaL_pushresult(b);
			///#endif // #if 0
		}
		return 1;
	}

	private static int os_time(LuaStateObject L) {
		DateTimeProxy t = new DateTimeProxy();
		if (Lua.lua_isnoneornil(L, 1)) { // called without args? 
			t.setNow(); // get current time 
		}
		else {
			LuaAuxLib.luaL_checktype(L, 1, Lua.LUA_TTABLE);
			LuaAPI.lua_settop(L, 1); // make sure table is at the top 
			int sec = getfield(L, CharPtr.toCharPtr("sec"), 0);
			int min = getfield(L, CharPtr.toCharPtr("min"), 0);
			int hour = getfield(L, CharPtr.toCharPtr("hour"), 12);
			int day = getfield(L, CharPtr.toCharPtr("day"), -1);
			int month = getfield(L, CharPtr.toCharPtr("month"), -1) - 1;
			int year = getfield(L, CharPtr.toCharPtr("year"), -1) - 1900;
			int isdst = getboolfield(L, CharPtr.toCharPtr("isdst")); // todo: implement this - mjf
			t = new DateTimeProxy(year, month, day, hour, min, sec);
		}
		LuaAPI.lua_pushnumber(L, t.getTicks());
		return 1;
	}

	private static int os_difftime(LuaStateObject L) {
		long ticks = (long)LuaAuxLib.luaL_checknumber(L, 1) - (long)LuaAuxLib.luaL_optnumber(L, 2, 0);
		LuaAPI.lua_pushnumber(L, ticks / 10000000); //FIXME: ticks / TimeSpan.TicksPerSecond
		return 1;
	}

	// }====================================================== 

	// locale not supported yet
	private static int os_setlocale(LuaStateObject L) {
//            
//		  static string[] cat = {LC_ALL, LC_COLLATE, LC_CTYPE, LC_MONETARY,
//							  LC_NUMERIC, LC_TIME};
//		  static string[] catnames[] = {"all", "collate", "ctype", "monetary",
//			 "numeric", "time", null};
//		  CharPtr l = luaL_optstring(L, 1, null);
//		  int op = luaL_checkoption(L, 2, "all", catnames);
//		  lua_pushstring(L, setlocale(cat[op], l));
//			 
		CharPtr l = LuaAuxLib.luaL_optstring(L, 1, null);
		LuaAPI.lua_pushstring(L, CharPtr.toCharPtr("C"));
		return (l.toString().equals("C")) ? 1 : 0;
	}

	private static int os_exit(LuaStateObject L) {
		System.exit(LuaConf.EXIT_SUCCESS);
		return 0;
	}

	private final static LuaLReg[] syslib = { new LuaLReg(CharPtr.toCharPtr("clock"), new LuaOSLib_delegate("os_clock")), new LuaLReg(CharPtr.toCharPtr("date"), new LuaOSLib_delegate("os_date")), new LuaLReg(CharPtr.toCharPtr("difftime"), new LuaOSLib_delegate("os_difftime")), new LuaLReg(CharPtr.toCharPtr("execute"), new LuaOSLib_delegate("os_execute")), new LuaLReg(CharPtr.toCharPtr("exit"), new LuaOSLib_delegate("os_exit")), new LuaLReg(CharPtr.toCharPtr("getenv"), new LuaOSLib_delegate("os_getenv")), new LuaLReg(CharPtr.toCharPtr("remove"), new LuaOSLib_delegate("os_remove")), new LuaLReg(CharPtr.toCharPtr("rename"), new LuaOSLib_delegate("os_rename")), new LuaLReg(CharPtr.toCharPtr("setlocale"), new LuaOSLib_delegate("os_setlocale")), new LuaLReg(CharPtr.toCharPtr("time"), new LuaOSLib_delegate("os_time")), new LuaLReg(CharPtr.toCharPtr("tmpname"), new LuaOSLib_delegate("os_tmpname")), new LuaLReg(null, null) };

	public static class LuaOSLib_delegate implements LuaCFunction {
		private String name;

		public LuaOSLib_delegate(String name) {
			this.name = name;
		}

		public final int exec(LuaStateObject L) {
			if ((new String("os_clock")).equals(name)) {
				return os_clock(L);
			}
			else if ((new String("os_date")).equals(name)) {
				return os_date(L);
			}
			else if ((new String("os_difftime")).equals(name)) {
				return os_difftime(L);
			}
			else if ((new String("os_execute")).equals(name)) {
				return os_execute(L);
			}
			else if ((new String("os_exit")).equals(name)) {
				return os_exit(L);
			}
			else if ((new String("os_getenv")).equals(name)) {
				return os_getenv(L);
			}
			else if ((new String("os_remove")).equals(name)) {
				return os_remove(L);
			}
			else if ((new String("os_rename")).equals(name)) {
				return os_rename(L);
			}
			else if ((new String("os_setlocale")).equals(name)) {
				return os_setlocale(L);
			}
			else if ((new String("os_time")).equals(name)) {
				return os_time(L);
			}
			else if ((new String("os_tmpname")).equals(name)) {
				return os_tmpname(L);
			}
			else {
				return 0;
			}
		}
	}


	// }====================================================== 

	public static int luaopen_os(LuaStateObject L) {
		LuaAuxLib.luaL_register(L, CharPtr.toCharPtr(LuaLib.LUA_OSLIBNAME), syslib);
		return 1;
	}
}