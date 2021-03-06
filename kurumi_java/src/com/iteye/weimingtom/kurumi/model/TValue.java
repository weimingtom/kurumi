﻿package com.iteye.weimingtom.kurumi.model;

import com.iteye.weimingtom.kurumi.port.ClassType;
import com.iteye.weimingtom.kurumi.proto.ArrayElement;

//
// ** $Id: lobject.c,v 2.22.1.1 2007/12/27 13:02:25 roberto Exp $
// ** Some generic functions over Lua objects
// ** See Copyright Notice in lua.h
// 

public class TValue implements ArrayElement {
	private TValue[] values = null;
	private int index = -1;

	public Value value = new Value();
	public int tt;

	public final void set_index(int index) {
		this.index = index;
	}

	public final void set_array(Object array) {
		this.values = (TValue[])array;
		ClassType.Assert(this.values != null);
	}

	//TValue this[int offset] get
	public final TValue get(int offset) {
		return this.values[this.index + offset];
	}

	//TValue this[uint offset] get
	//public TValue get(uint offset)
	//{
	//	return this.values[this.index + (int)offset];
	//}

	public static TValue plus(TValue value, int offset) {
		return value.values[value.index + offset];
	}

	//operator +
	public static TValue plus(int offset, TValue value) {
		return value.values[value.index + offset];
	}

	public static TValue minus(TValue value, int offset) {
		return value.values[value.index - offset];
	}

	//operator -
	public static int minus(TValue value, TValue[] array) {
		ClassType.Assert(value.values == array);
		return value.index;
	}

	//operator -
	public static int minus(TValue a, TValue b) {
		ClassType.Assert(a.values == b.values);
		return a.index - b.index;
	}

	//operator <
	public static boolean lessThan(TValue a, TValue b) {
		ClassType.Assert(a.values == b.values);
		return a.index < b.index;
	}

	//operator <=
	public static boolean lessEqual(TValue a, TValue b) {
		ClassType.Assert(a.values == b.values);
		return a.index <= b.index;
	}

	//operator >
	public static boolean greaterThan(TValue a, TValue b) {
		ClassType.Assert(a.values == b.values);
		return a.index > b.index;
	}

	//operator >=
	public static boolean greaterEqual(TValue a, TValue b) {
		ClassType.Assert(a.values == b.values);
		return a.index >= b.index;
	}

	public static TValue inc(TValue[] value) { //ref
		value[0] = value[0].get(1);
		return value[0].get(-1);
	}

	public static TValue dec(TValue[] value) { //ref
		value[0] = value[0].get(-1);
		return value[0].get(1);
	}

	//implicit operator int
	public static int toInt(TValue value) {
		return value.index;
	}

	public TValue() {
		this.values = null;
		this.index = 0;
		this.value = new Value();
		this.tt = 0;
	}

	public TValue(TValue value) {
		this.values = value.values;
		this.index = value.index;
		this.value = new Value(value.value); // todo: do a shallow copy here
		this.tt = value.tt;
	}

	//public TValue(TValue[] values)
	//{
	//	this.values = values;
	//	this.index = Array.IndexOf(values, this);
	//	this.value = new Value();
	//	this.tt = 0;
	//}

	public TValue(Value value, int tt) {
		this.values = null;
		this.index = 0;
		this.value = new Value(value);
		this.tt = tt;
	}

	//public TValue(TValue[] values, Value valueCls, int tt)
	//{
	//	this.values = values;
	//	this.index = Array.IndexOf(values, this);
	//	this.value = new Value(valueCls);
	//	this.tt = tt;
	//}
}