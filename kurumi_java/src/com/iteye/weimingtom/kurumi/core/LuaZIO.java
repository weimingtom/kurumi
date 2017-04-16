package com.iteye.weimingtom.kurumi.core;

import com.iteye.weimingtom.kurumi.func.LuaReader;
import com.iteye.weimingtom.kurumi.model.CharPtr;
import com.iteye.weimingtom.kurumi.model.LuaStateObject;
import com.iteye.weimingtom.kurumi.model.Mbuffer;
import com.iteye.weimingtom.kurumi.model.ZIO;
import com.iteye.weimingtom.kurumi.port.ClassType;

//
// ** $Id: lzio.c,v 1.31.1.1 2007/12/27 13:02:25 roberto Exp $
// ** a generic input stream interface
// ** See Copyright Notice in lua.h
// 
public class LuaZIO {
	public static final int EOZ = -1; // end of stream 

	public static int char2int(char c) {
		return (int)c;
	}

	public static int zgetc(ZIO z) {
		if (z.n-- > 0) {
			int ch = char2int(z.p.get(0));
			z.p.inc();
			return ch;
		}
		else {
			return luaZ_fill(z);
		}
	}

	public static void luaZ_initbuffer(LuaStateObject L, Mbuffer buff) {
		buff.buffer = null;
	}

	public static CharPtr luaZ_buffer(Mbuffer buff) {
		return buff.buffer;
	}

	public static int luaZ_sizebuffer(Mbuffer buff) { //uint
		return buff.buffsize;
	}

	public static int luaZ_bufflen(Mbuffer buff) { //uint
		return buff.n;
	}

	public static void luaZ_resetbuffer(Mbuffer buff) {
		buff.n = 0;
	}

	public static void luaZ_resizebuffer(LuaStateObject L, Mbuffer buff, int size) {
		if (CharPtr.isEqual(buff.buffer, null)) {
			buff.buffer = new CharPtr();
		}
		char[][] chars_ref = new char[1][];
		chars_ref[0] = buff.buffer.chars;
		LuaMem.luaM_reallocvector_char(L, chars_ref, buff.buffsize, size, new ClassType(ClassType.TYPE_CHAR)); //(int) - ref
		buff.buffer.chars = chars_ref[0];
		buff.buffsize = buff.buffer.chars.length; //(uint)
	}

	public static void luaZ_freebuffer(LuaStateObject L, Mbuffer buff) {
		luaZ_resizebuffer(L, buff, 0);
	}

	// --------- Private Part ------------------ 

	public static int luaZ_fill(ZIO z) {
		int[] size = new int[1]; //uint
		LuaStateObject L = z.L;
		CharPtr buff;
		LuaLimits.lua_unlock(L);
		buff = z.reader.exec(L, z.data, size); //out
		LuaLimits.lua_lock(L);
		if (CharPtr.isEqual(buff, null) || size[0] == 0) {
			return EOZ;
		}
		z.n = size[0] - 1;
		z.p = new CharPtr(buff);
		int result = char2int(z.p.get(0));
		z.p.inc();
		return result;
	}

	public static int luaZ_lookahead(ZIO z) {
		if (z.n == 0) {
			if (luaZ_fill(z) == EOZ) {
				return EOZ;
			}
			else {
				z.n++; // luaZ_fill removed first byte; put back it 
				z.p.dec();
			}
		}
		return char2int(z.p.get(0));
	}

	public static void luaZ_init(LuaStateObject L, ZIO z, LuaReader reader, Object data) {
		z.L = L;
		z.reader = reader;
		z.data = data;
		z.n = 0;
		z.p = null;
	}

	// --------------------------------------------------------------- read --- 
	public static int luaZ_read(ZIO z, CharPtr b, int n) { //uint - uint
		b = new CharPtr(b);
		while (n != 0) {
			int m; //uint
			if (luaZ_lookahead(z) == EOZ) {
				return n; // return number of missing bytes
			}
			m = (n <= z.n) ? n : z.n; // min. between n and z.n
			LuaConf.memcpy(b, z.p, m);
			z.n -= m;
			z.p = CharPtr.plus(z.p, m);
			b = CharPtr.plus(b, m);
			n -= m;
		}
		return 0;
	}

	// ------------------------------------------------------------------------ 
	public static CharPtr luaZ_openspace(LuaStateObject L, Mbuffer buff, int n) { //uint
		if (n > buff.buffsize) {
			if (n < LuaLimits.LUA_MINBUFFER) {
				n = LuaLimits.LUA_MINBUFFER;
			}
			luaZ_resizebuffer(L, buff, n); //(int)
		}
		return buff.buffer;
	}
}