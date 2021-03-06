﻿package com.iteye.weimingtom.kurumi.util;

import com.iteye.weimingtom.kurumi.constant.OpCode;

public class OpCodeUtil {
	public static long opCodeToLong(OpCode code) {
		switch (code) {
			case OP_MOVE:
				return 0;
			case OP_LOADK:
				return 1;
			case OP_LOADBOOL:
				return 2;
			case OP_LOADNIL:
				return 3;
			case OP_GETUPVAL:
				return 4;
			case OP_GETGLOBAL:
				return 5;
			case OP_GETTABLE:
				return 6;
			case OP_SETGLOBAL:
				return 7;
			case OP_SETUPVAL:
				return 8;
			case OP_SETTABLE:
				return 9;
			case OP_NEWTABLE:
				return 10;
			case OP_SELF:
				return 11;
			case OP_ADD:
				return 12;
			case OP_SUB:
				return 13;
			case OP_MUL:
				return 14;
			case OP_DIV:
				return 15;
			case OP_MOD:
				return 16;
			case OP_POW:
				return 17;
			case OP_UNM:
				return 18;
			case OP_NOT:
				return 19;
			case OP_LEN:
				return 20;
			case OP_CONCAT:
				return 21;
			case OP_JMP:
				return 22;
			case OP_EQ:
				return 23;
			case OP_LT:
				return 24;
			case OP_LE:
				return 25;
			case OP_TEST:
				return 26;
			case OP_TESTSET:
				return 27;
			case OP_CALL:
				return 28;
			case OP_TAILCALL:
				return 29;
			case OP_RETURN:
				return 30;
			case OP_FORLOOP:
				return 31;
			case OP_FORPREP:
				return 32;
			case OP_TFORLOOP:
				return 33;
			case OP_SETLIST:
				return 34;
			case OP_CLOSE:
				return 35;
			case OP_CLOSURE:
				return 36;
			case OP_VARARG:
				return 37;
		}
		throw new RuntimeException("OpCode error");
	}

	public static OpCode longToOpCode(long code) {
		switch ((int)code) {
			case 0:
				return OpCode.OP_MOVE;
			case 1:
				return OpCode.OP_LOADK;
			case 2:
				return OpCode.OP_LOADBOOL;
			case 3:
				return OpCode.OP_LOADNIL;
			case 4:
				return OpCode.OP_GETUPVAL;
			case 5:
				return OpCode.OP_GETGLOBAL;
			case 6:
				return OpCode.OP_GETTABLE;
			case 7:
				return OpCode.OP_SETGLOBAL;
			case 8:
				return OpCode.OP_SETUPVAL;
			case 9:
				return OpCode.OP_SETTABLE;
			case 10:
				return OpCode.OP_NEWTABLE;
			case 11:
				return OpCode.OP_SELF;
			case 12:
				return OpCode.OP_ADD;
			case 13:
				return OpCode.OP_SUB;
			case 14:
				return OpCode.OP_MUL;
			case 15:
				return OpCode.OP_DIV;
			case 16:
				return OpCode.OP_MOD;
			case 17:
				return OpCode.OP_POW;
			case 18:
				return OpCode.OP_UNM;
			case 19:
				return OpCode.OP_NOT;
			case 20:
				return OpCode.OP_LEN;
			case 21:
				return OpCode.OP_CONCAT;
			case 22:
				return OpCode.OP_JMP;
			case 23:
				return OpCode.OP_EQ;
			case 24:
				return OpCode.OP_LT;
			case 25:
				return OpCode.OP_LE;
			case 26:
				return OpCode.OP_TEST;
			case 27:
				return OpCode.OP_TESTSET;
			case 28:
				return OpCode.OP_CALL;
			case 29:
				return OpCode.OP_TAILCALL;
			case 30:
				return OpCode.OP_RETURN;
			case 31:
				return OpCode.OP_FORLOOP;
			case 32:
				return OpCode.OP_FORPREP;
			case 33:
				return OpCode.OP_TFORLOOP;
			case 34:
				return OpCode.OP_SETLIST;
			case 35:
				return OpCode.OP_CLOSE;
			case 36:
				return OpCode.OP_CLOSURE;
			case 37:
				return OpCode.OP_VARARG;
		}
		throw new RuntimeException("OpCode error");
	}
}