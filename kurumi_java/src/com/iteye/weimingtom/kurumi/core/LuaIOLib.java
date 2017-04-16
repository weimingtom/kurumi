package com.iteye.weimingtom.kurumi.core;

import com.iteye.weimingtom.kurumi.func.LuaCFunction;
import com.iteye.weimingtom.kurumi.model.CharPtr;
import com.iteye.weimingtom.kurumi.model.FilePtr;
import com.iteye.weimingtom.kurumi.model.LuaLBuffer;
import com.iteye.weimingtom.kurumi.model.LuaLReg;
import com.iteye.weimingtom.kurumi.model.LuaStateObject;
import com.iteye.weimingtom.kurumi.port.ClassType;
import com.iteye.weimingtom.kurumi.port.StreamProxy;

//
// ** $Id: liolib.c,v 2.73.1.3 2008/01/18 17:47:43 roberto Exp $
// ** Standard I/O (and system) library
// ** See Copyright Notice in lua.h
// 
//using lua_Number = System.Double;
//using lua_Integer = System.Int32;

public class LuaIOLib {
	public static final int IO_INPUT = 1;
	public static final int IO_OUTPUT = 2;

	private static final String[] fnames = { "input", "output" };

	private static int pushresult(LuaStateObject L, int i, CharPtr filename) {
		int en = LuaConf.errno(); // calls to Lua API may change this value 
		if (i != 0) {
			LuaAPI.lua_pushboolean(L, 1);
			return 1;
		}
		else {
			LuaAPI.lua_pushnil(L);
			if (CharPtr.isNotEqual(filename, null)) {
				LuaAPI.lua_pushfstring(L, CharPtr.toCharPtr("%s: %s"), filename, LuaConf.strerror(en));
			}
			else {
				LuaAPI.lua_pushfstring(L, CharPtr.toCharPtr("%s"), LuaConf.strerror(en));
			}
			LuaAPI.lua_pushinteger(L, en);
			return 3;
		}
	}

	private static void fileerror(LuaStateObject L, int arg, CharPtr filename) {
		LuaAPI.lua_pushfstring(L, CharPtr.toCharPtr("%s: %s"), filename, LuaConf.strerror(LuaConf.errno()));
		LuaAuxLib.luaL_argerror(L, arg, Lua.lua_tostring(L, -1));
	}

	public static FilePtr tofilep(LuaStateObject L) {
		return (FilePtr)LuaAuxLib.luaL_checkudata(L, 1, CharPtr.toCharPtr(LuaLib.LUA_FILEHANDLE));
	}

	private static int io_type(LuaStateObject L) {
		Object ud;
		LuaAuxLib.luaL_checkany(L, 1);
		ud = LuaAPI.lua_touserdata(L, 1);
		LuaAPI.lua_getfield(L, Lua.LUA_REGISTRYINDEX, CharPtr.toCharPtr(LuaLib.LUA_FILEHANDLE));
		if (ud == null || (LuaAPI.lua_getmetatable(L, 1) == 0) || (LuaAPI.lua_rawequal(L, -2, -1) == 0)) {
			LuaAPI.lua_pushnil(L); // not a file 
		}
		else if (((FilePtr)((ud instanceof FilePtr) ? ud : null)).file == null) {
			Lua.lua_pushliteral(L, CharPtr.toCharPtr("closed file"));
		}
		else {
			Lua.lua_pushliteral(L, CharPtr.toCharPtr("file"));
		}
		return 1;
	}

	private static StreamProxy tofile(LuaStateObject L) {
		FilePtr f = tofilep(L);
		if (f.file == null) {
			LuaAuxLib.luaL_error(L, CharPtr.toCharPtr("attempt to use a closed file"));
		}
		return f.file;
	}

//        
//		 ** When creating file files, always creates a `closed' file file
//		 ** before opening the actual file; so, if there is a memory error, the
//		 ** file is not left opened.
//		 
	private static FilePtr newfile(LuaStateObject L) {
		FilePtr pf = (FilePtr)LuaAPI.lua_newuserdata(L, new ClassType(ClassType.TYPE_FILEPTR)); //FilePtr
		pf.file = null; // file file is currently `closed' 
		LuaAuxLib.luaL_getmetatable(L, CharPtr.toCharPtr(LuaLib.LUA_FILEHANDLE));
		LuaAPI.lua_setmetatable(L, -2);
		return pf;
	}

//        
//		 ** function to (not) close the standard files stdin, stdout, and stderr
//		 
	private static int io_noclose(LuaStateObject L) {
		LuaAPI.lua_pushnil(L);
		Lua.lua_pushliteral(L, CharPtr.toCharPtr("cannot close standard file"));
		return 2;
	}

//        
//		 ** function to close 'popen' files
//		 
	private static int io_pclose(LuaStateObject L) {
		FilePtr p = tofilep(L);
		int ok = (LuaConf.lua_pclose(L, p.file) == 0) ? 1 : 0;
		p.file = null;
		return pushresult(L, ok, null);
	}

//        
//		 ** function to close regular files
//		 
	private static int io_fclose(LuaStateObject L) {
		FilePtr p = tofilep(L);
		int ok = (LuaConf.fclose(p.file) == 0) ? 1 : 0;
		p.file = null;
		return pushresult(L, ok, null);
	}

	private static int aux_close(LuaStateObject L) {
		LuaAPI.lua_getfenv(L, 1);
		LuaAPI.lua_getfield(L, -1, CharPtr.toCharPtr("__close"));
		return (LuaAPI.lua_tocfunction(L, -1)).exec(L);
	}

	private static int io_close(LuaStateObject L) {
		if (Lua.lua_isnone(L, 1)) {
			LuaAPI.lua_rawgeti(L, Lua.LUA_ENVIRONINDEX, IO_OUTPUT);
		}
		tofile(L); // make sure argument is a file 
		return aux_close(L);
	}

	private static int io_gc(LuaStateObject L) {
		StreamProxy f = tofilep(L).file;
		// ignore closed files 
		if (f != null) {
			aux_close(L);
		}
		return 0;
	}

	private static int io_tostring(LuaStateObject L) {
		StreamProxy f = tofilep(L).file;
		if (f == null) {
			Lua.lua_pushliteral(L, CharPtr.toCharPtr("file (closed)"));
		}
		else {
			LuaAPI.lua_pushfstring(L, CharPtr.toCharPtr("file (%p)"), f);
		}
		return 1;
	}

	private static int io_open(LuaStateObject L) {
		CharPtr filename = LuaAuxLib.luaL_checkstring(L, 1);
		CharPtr mode = LuaAuxLib.luaL_optstring(L, 2, CharPtr.toCharPtr("r"));
		FilePtr pf = newfile(L);
		pf.file = LuaConf.fopen(filename, mode);
		return (pf.file == null) ? pushresult(L, 0, filename) : 1;
	}

//        
//		 ** this function has a separated environment, which defines the
//		 ** correct __close for 'popen' files
//		 
	private static int io_popen(LuaStateObject L) {
		CharPtr filename = LuaAuxLib.luaL_checkstring(L, 1);
		CharPtr mode = LuaAuxLib.luaL_optstring(L, 2, CharPtr.toCharPtr("r"));
		FilePtr pf = newfile(L);
		pf.file = LuaConf.lua_popen(L, filename, mode);
		return (pf.file == null) ? pushresult(L, 0, filename) : 1;
	}

	private static int io_tmpfile(LuaStateObject L) {
		FilePtr pf = newfile(L);
		pf.file = LuaConf.tmpfile();
		return (pf.file == null) ? pushresult(L, 0, null) : 1;
	}

	private static StreamProxy getiofile(LuaStateObject L, int findex) {
		StreamProxy f;
		LuaAPI.lua_rawgeti(L, Lua.LUA_ENVIRONINDEX, findex);
		Object tempVar = LuaAPI.lua_touserdata(L, -1);
		f = ((FilePtr)((tempVar instanceof FilePtr) ? tempVar : null)).file;
		if (f == null) {
			LuaAuxLib.luaL_error(L, CharPtr.toCharPtr("standard %s file is closed"), fnames[findex - 1]);
		}
		return f;
	}

	private static int g_iofile(LuaStateObject L, int f, CharPtr mode) {
		if (!Lua.lua_isnoneornil(L, 1)) {
			CharPtr filename = Lua.lua_tostring(L, 1);
			if (CharPtr.isNotEqual(filename, null)) {
				FilePtr pf = newfile(L);
				pf.file = LuaConf.fopen(filename, mode);
				if (pf.file == null) {
					fileerror(L, 1, filename);
				}
			}
			else {
				tofile(L); // check that it's a valid file file 
				LuaAPI.lua_pushvalue(L, 1);
			}
			LuaAPI.lua_rawseti(L, Lua.LUA_ENVIRONINDEX, f);
		}
		// return current value 
		LuaAPI.lua_rawgeti(L, Lua.LUA_ENVIRONINDEX, f);
		return 1;
	}

	private static int io_input(LuaStateObject L) {
		return g_iofile(L, IO_INPUT, CharPtr.toCharPtr("r"));
	}

	private static int io_output(LuaStateObject L) {
		return g_iofile(L, IO_OUTPUT, CharPtr.toCharPtr("w"));
	}

	private static void aux_lines(LuaStateObject L, int idx, int toclose) {
		LuaAPI.lua_pushvalue(L, idx);
		LuaAPI.lua_pushboolean(L, toclose); // close/not close file when finished 
		LuaAPI.lua_pushcclosure(L, new LuaIOLib_delegate("io_readline"), 2);
	}

	private static int f_lines(LuaStateObject L) {
		tofile(L); // check that it's a valid file file 
		aux_lines(L, 1, 0);
		return 1;
	}

	private static int io_lines(LuaStateObject L) {
		if (Lua.lua_isnoneornil(L, 1)) {
			// no arguments? 
			// will iterate over default input 
			LuaAPI.lua_rawgeti(L, Lua.LUA_ENVIRONINDEX, IO_INPUT);
			return f_lines(L);
		}
		else {
			CharPtr filename = LuaAuxLib.luaL_checkstring(L, 1);
			FilePtr pf = newfile(L);
			pf.file = LuaConf.fopen(filename, CharPtr.toCharPtr("r"));
			if (pf.file == null) {
				fileerror(L, 1, filename);
			}
			aux_lines(L, LuaAPI.lua_gettop(L), 1);
			return 1;
		}
	}


//        
//		 ** {======================================================
//		 ** READ
//		 ** =======================================================
//		 

	private static int read_number(LuaStateObject L, StreamProxy f) {
		//lua_Number d;
		Object[] parms = {(Object)(double)0.0};
		if (LuaConf.fscanf(f, CharPtr.toCharPtr(LuaConf.LUA_NUMBER_SCAN), parms) == 1) {
			LuaAPI.lua_pushnumber(L, ((Double)parms[0]).doubleValue());
			return 1;
		}
		else {
			return 0; // read fails 
		}
	}

	private static int test_eof(LuaStateObject L, StreamProxy f) {
		int c = LuaConf.getc(f);
		LuaConf.ungetc(c, f);
		LuaAPI.lua_pushlstring(L, null, 0);
		return (c != LuaConf.EOF) ? 1 : 0;
	}

	private static int read_line(LuaStateObject L, StreamProxy f) {
		LuaLBuffer b = new LuaLBuffer();
		LuaAuxLib.luaL_buffinit(L, b);
		for (;;) {
			int l; //uint
			CharPtr p = LuaAuxLib.luaL_prepbuffer(b);
			if (CharPtr.isEqual(LuaConf.fgets(p, f), null)) {
				// eof? 
				LuaAuxLib.luaL_pushresult(b); // close buffer 
				return (LuaAPI.lua_objlen(L, -1) > 0) ? 1 : 0; // check whether read something 
			}
			l = (int)LuaConf.strlen(p); //uint
			if (l == 0 || p.get(l - 1) != '\n') {
				LuaAuxLib.luaL_addsize(b, (int)l);
			}
			else {
				LuaAuxLib.luaL_addsize(b, (int)(l - 1)); // do not include `eol' 
				LuaAuxLib.luaL_pushresult(b); // close buffer 
				return 1; // read at least an `eol' 
			}
		}
	}

	private static int read_chars(LuaStateObject L, StreamProxy f, long n) { //uint
		long rlen; // how much to read  - uint
		int nr; // number of chars actually read  - uint
		LuaLBuffer b = new LuaLBuffer();
		LuaAuxLib.luaL_buffinit(L, b);
		rlen = LuaConf.LUAL_BUFFERSIZE; // try to read that much each time 
		do {
			CharPtr p = LuaAuxLib.luaL_prepbuffer(b);
			if (rlen > n) {
				rlen = n; // cannot read more than asked 
			}
			nr = (int)LuaConf.fread(p, LuaConf.GetUnmanagedSize(new ClassType(ClassType.TYPE_CHAR)), (int)rlen, f); //typeof(char) - uint
			LuaAuxLib.luaL_addsize(b, (int)nr);
			n -= nr; // still have to read `n' chars 
		} while (n > 0 && nr == rlen); // until end of count or eof 
		LuaAuxLib.luaL_pushresult(b); // close buffer 
		return (n == 0 || LuaAPI.lua_objlen(L, -1) > 0) ? 1 : 0;
	}

	private static int g_read(LuaStateObject L, StreamProxy f, int first) {
		int nargs = LuaAPI.lua_gettop(L) - 1;
		int success;
		int n;
		LuaConf.clearerr(f);
		if (nargs == 0) {
			// no arguments? 
			success = read_line(L, f);
			n = first + 1; // to return 1 result 
		}
		else {
			// ensure stack space for all results and for auxlib's buffer 
			LuaAuxLib.luaL_checkstack(L, nargs + Lua.LUA_MINSTACK, CharPtr.toCharPtr("too many arguments"));
			success = 1;
			for (n = first; (nargs-- != 0) && (success != 0); n++) {
				if (LuaAPI.lua_type(L, n) == Lua.LUA_TNUMBER) {
					int l = (int)LuaAPI.lua_tointeger(L, n); //uint - uint
					success = (l == 0) ? test_eof(L, f) : read_chars(L, f, l);
				}
				else {
					CharPtr p = Lua.lua_tostring(L, n);
					LuaAuxLib.luaL_argcheck(L, (CharPtr.isNotEqual(p, null)) && (p.get(0) == '*'), n, "invalid option");
					switch (p.get(1)) {
						case 'n': { // number 
								success = read_number(L, f);
								break;
							}
						case 'l': { // line 
								success = read_line(L, f);
								break;
							}
						case 'a': { // file 
							read_chars(L, f, (long)((~(int)0) & 0xffffffff)); // read MAX_uint chars  - ~((uint)0
								success = 1; // always success 
								break;
							}
						default: {
								return LuaAuxLib.luaL_argerror(L, n, CharPtr.toCharPtr("invalid format"));
							}
					}
				}
			}
		}
		if (LuaConf.ferror(f) != 0) {
			return pushresult(L, 0, null);
		}
		if (success == 0) {
			Lua.lua_pop(L, 1); // remove last result 
			LuaAPI.lua_pushnil(L); // push nil instead 
		}
		return n - first;
	}

	private static int io_read(LuaStateObject L) {
		return g_read(L, getiofile(L, IO_INPUT), 1);
	}

	private static int f_read(LuaStateObject L) {
		return g_read(L, tofile(L), 2);
	}

	private static int io_readline(LuaStateObject L) {
		Object tempVar = LuaAPI.lua_touserdata(L, Lua.lua_upvalueindex(1));
		StreamProxy f = ((FilePtr)((tempVar instanceof FilePtr) ? tempVar : null)).file;
		int sucess;
		if (f == null) { // file is already closed? 
			LuaAuxLib.luaL_error(L, CharPtr.toCharPtr("file is already closed"));
		}
		sucess = read_line(L, f);
		if (LuaConf.ferror(f) != 0) {
			return LuaAuxLib.luaL_error(L, CharPtr.toCharPtr("%s"), LuaConf.strerror(LuaConf.errno()));
		}
		if (sucess != 0) {
			return 1;
		}
		else {
			// EOF 
			if (LuaAPI.lua_toboolean(L, Lua.lua_upvalueindex(2)) != 0) {
				// generator created file? 
				LuaAPI.lua_settop(L, 0);
				LuaAPI.lua_pushvalue(L, Lua.lua_upvalueindex(1));
				aux_close(L); // close it 
			}
			return 0;
		}
	}

	// }====================================================== 

	private static int g_write(LuaStateObject L, StreamProxy f, int arg) {
		int nargs = LuaAPI.lua_gettop(L) - 1;
		int status = 1;
		for (; (nargs--) != 0; arg++) {
			if (LuaAPI.lua_type(L, arg) == Lua.LUA_TNUMBER) {
				// optimization: could be done exactly as for strings 
				status = ((status!=0) && (LuaConf.fprintf(f, CharPtr.toCharPtr(LuaConf.LUA_NUMBER_FMT), LuaAPI.lua_tonumber(L, arg)) > 0)) ? 1 : 0;
			}
			else {
				int[] l = new int[1]; //uint
				CharPtr s = LuaAuxLib.luaL_checklstring(L, arg, l); //out
				status = ((status != 0) && (LuaConf.fwrite(s, LuaConf.GetUnmanagedSize(new ClassType(ClassType.TYPE_CHAR)), (int)l[0], f) == l[0])) ? 1 : 0; //typeof(char)
			}
		}
		return pushresult(L, status, null);
	}

	private static int io_write(LuaStateObject L) {
		return g_write(L, getiofile(L, IO_OUTPUT), 1);
	}

	private static int f_write(LuaStateObject L) {
		return g_write(L, tofile(L), 2);
	}

	private static int f_seek(LuaStateObject L) {
		int[] mode = { LuaConf.SEEK_SET, LuaConf.SEEK_CUR, LuaConf.SEEK_END };
		CharPtr[] modenames = { CharPtr.toCharPtr("set"), CharPtr.toCharPtr("cur"), CharPtr.toCharPtr("end"), null };
		StreamProxy f = tofile(L);
		int op = LuaAuxLib.luaL_checkoption(L, 2, CharPtr.toCharPtr("cur"), modenames);
		long offset = LuaAuxLib.luaL_optlong(L, 3, 0);
		op = LuaConf.fseek(f, offset, mode[op]);
		if (op != 0) {
			return pushresult(L, 0, null); // error 
		}
		else {
			LuaAPI.lua_pushinteger(L, LuaConf.ftell(f));
			return 1;
		}
	}

	private static int f_setvbuf(LuaStateObject L) {
		CharPtr[] modenames = { CharPtr.toCharPtr("no"), CharPtr.toCharPtr("full"), CharPtr.toCharPtr("line"), null };
		int[] mode = { LuaConf._IONBF, LuaConf._IOFBF, LuaConf._IOLBF };
		StreamProxy f = tofile(L);
		int op = LuaAuxLib.luaL_checkoption(L, 2, null, modenames);
		int sz = LuaAuxLib.luaL_optinteger(L, 3, LuaConf.LUAL_BUFFERSIZE); //lua_Integer - Int32
		int res = LuaConf.setvbuf(f, null, mode[op], (int)sz); //uint
		return pushresult(L, (res == 0) ? 1 : 0, null);
	}

	private static int io_flush(LuaStateObject L) {
		int result = 1;
		try {
			getiofile(L, IO_OUTPUT).Flush();
		}
		catch (java.lang.Exception e) {
			result = 0;
		}
		return pushresult(L, result, null);
	}

	private static int f_flush(LuaStateObject L) {
		int result = 1;
		try {
			tofile(L).Flush();
		}
		catch (java.lang.Exception e) {
			result = 0;
		}
		return pushresult(L, result, null);
	}


	private final static LuaLReg[] iolib = { new LuaLReg(CharPtr.toCharPtr("close"), new LuaIOLib_delegate("io_close")), new LuaLReg(CharPtr.toCharPtr("flush"), new LuaIOLib_delegate("io_flush")), new LuaLReg(CharPtr.toCharPtr("input"), new LuaIOLib_delegate("io_input")), new LuaLReg(CharPtr.toCharPtr("lines"), new LuaIOLib_delegate("io_lines")), new LuaLReg(CharPtr.toCharPtr("open"), new LuaIOLib_delegate("io_open")), new LuaLReg(CharPtr.toCharPtr("output"), new LuaIOLib_delegate("io_output")), new LuaLReg(CharPtr.toCharPtr("popen"), new LuaIOLib_delegate("io_popen")), new LuaLReg(CharPtr.toCharPtr("read"), new LuaIOLib_delegate("io_read")), new LuaLReg(CharPtr.toCharPtr("tmpfile"), new LuaIOLib_delegate("io_tmpfile")), new LuaLReg(CharPtr.toCharPtr("type"), new LuaIOLib_delegate("io_type")), new LuaLReg(CharPtr.toCharPtr("write"), new LuaIOLib_delegate("io_write")), new LuaLReg(null, null) };

	private final static LuaLReg[] flib = { new LuaLReg(CharPtr.toCharPtr("close"), new LuaIOLib_delegate("io_close")), new LuaLReg(CharPtr.toCharPtr("flush"), new LuaIOLib_delegate("f_flush")), new LuaLReg(CharPtr.toCharPtr("lines"), new LuaIOLib_delegate("f_lines")), new LuaLReg(CharPtr.toCharPtr("read"), new LuaIOLib_delegate("f_read")), new LuaLReg(CharPtr.toCharPtr("seek"), new LuaIOLib_delegate("f_seek")), new LuaLReg(CharPtr.toCharPtr("setvbuf"), new LuaIOLib_delegate("f_setvbuf")), new LuaLReg(CharPtr.toCharPtr("write"), new LuaIOLib_delegate("f_write")), new LuaLReg(CharPtr.toCharPtr("__gc"), new LuaIOLib_delegate("io_gc")), new LuaLReg(CharPtr.toCharPtr("__tostring"), new LuaIOLib_delegate("io_tostring")), new LuaLReg(null, null) };

	public static class LuaIOLib_delegate implements LuaCFunction {
		private String name;

		public LuaIOLib_delegate(String name) {
			this.name = name;
		}

		public final int exec(LuaStateObject L) {
			if ((new String("io_close")).equals(name)) {
				return io_close(L);
			}
			else if ((new String("io_flush")).equals(name)) {
				return io_flush(L);
			}
			else if ((new String("io_input")).equals(name)) {
				return io_input(L);
			}
			else if ((new String("io_lines")).equals(name)) {
				return io_lines(L);
			}
			else if ((new String("io_open")).equals(name)) {
				return io_open(L);
			}
			else if ((new String("io_output")).equals(name)) {
				return io_output(L);
			}
			else if ((new String("io_popen")).equals(name)) {
				return io_popen(L);
			}
			else if ((new String("io_read")).equals(name)) {
				return io_read(L);
			}
			else if ((new String("io_tmpfile")).equals(name)) {
				return io_tmpfile(L);
			}
			else if ((new String("io_type")).equals(name)) {
				return io_type(L);
			}
			else if ((new String("io_write")).equals(name)) {
				return io_write(L);
			}
			else if ((new String("f_flush")).equals(name)) {
				return f_flush(L);
			}
			else if ((new String("f_lines")).equals(name)) {
				return f_lines(L);
			}
			else if ((new String("f_read")).equals(name)) {
				return f_read(L);
			}
			else if ((new String("f_seek")).equals(name)) {
				return f_seek(L);
			}
			else if ((new String("f_setvbuf")).equals(name)) {
				return f_setvbuf(L);
			}
			else if ((new String("f_write")).equals(name)) {
				return f_write(L);
			}
			else if ((new String("io_gc")).equals(name)) {
				return io_gc(L);
			}
			else if ((new String("io_tostring")).equals(name)) {
				 return io_tostring(L);
			}
			else if ((new String("io_fclose")).equals(name)) {
				return io_fclose(L);
			}
			else if ((new String("io_noclose")).equals(name)) {
				return io_noclose(L);
			}
			else if ((new String("io_pclose")).equals(name)) {
				return io_pclose(L);
			}
			else if ((new String("io_readline")).equals(name)) {
				return io_readline(L);
			}
			else {
				return 0;
			}
		}
	}

	private static void createmeta(LuaStateObject L) {
		LuaAuxLib.luaL_newmetatable(L, CharPtr.toCharPtr(LuaLib.LUA_FILEHANDLE)); // create metatable for file files 
		LuaAPI.lua_pushvalue(L, -1); // push metatable 
		LuaAPI.lua_setfield(L, -2, CharPtr.toCharPtr("__index")); // metatable.__index = metatable 
		LuaAuxLib.luaL_register(L, null, flib); // file methods 
	}

	private static void createstdfile(LuaStateObject L, StreamProxy f, int k, CharPtr fname) {
		newfile(L).file = f;
		if (k > 0) {
			LuaAPI.lua_pushvalue(L, -1);
			LuaAPI.lua_rawseti(L, Lua.LUA_ENVIRONINDEX, k);
		}
		LuaAPI.lua_pushvalue(L, -2); // copy environment 
		LuaAPI.lua_setfenv(L, -2); // set it 
		LuaAPI.lua_setfield(L, -3, fname);
	}

	private static void newfenv(LuaStateObject L, LuaCFunction cls) {
		LuaAPI.lua_createtable(L, 0, 1);
		Lua.lua_pushcfunction(L, cls);
		LuaAPI.lua_setfield(L, -2, CharPtr.toCharPtr("__close"));
	}

	public static int luaopen_io(LuaStateObject L) {
		createmeta(L);
		// create (private) environment (with fields IO_INPUT, IO_OUTPUT, __close) 
		newfenv(L, new LuaIOLib_delegate("io_fclose"));
		LuaAPI.lua_replace(L, Lua.LUA_ENVIRONINDEX);
		// open library 
		LuaAuxLib.luaL_register(L, CharPtr.toCharPtr(LuaLib.LUA_IOLIBNAME), iolib);
		// create (and set) default files 
		newfenv(L, new LuaIOLib_delegate("io_noclose")); // close function for default files 
		createstdfile(L, LuaConf.stdin, IO_INPUT, CharPtr.toCharPtr("stdin"));
		createstdfile(L, LuaConf.stdout, IO_OUTPUT, CharPtr.toCharPtr("stdout"));
		createstdfile(L, LuaConf.stderr, 0, CharPtr.toCharPtr("stderr"));
		Lua.lua_pop(L, 1); // pop environment for default files 
		LuaAPI.lua_getfield(L, -1, CharPtr.toCharPtr("popen"));
		newfenv(L, new LuaIOLib_delegate("io_pclose")); // create environment for 'popen' 
		LuaAPI.lua_setfenv(L, -2); // set fenv for 'popen' 
		Lua.lua_pop(L, 1); // pop 'popen' 
		return 1;
	}
}