@.HardV_vtable = global [0 x i8*] []
@.A_vtable = global [3 x i8*] [i8* bitcast (i32 (i8*)* @A.meth to i8*), i8* bitcast (i32 (i8*)* @A.bar to i8*), i8* bitcast (i32 (i8*)* @A.AAA to i8*)]
@.B_vtable = global [4 x i8*] [i8* bitcast (i32 (i8*)* @B.meth to i8*), i8* bitcast (i32 (i8*)* @A.bar to i8*), i8* bitcast (i32 (i8*)* @A.AAA to i8*), i8* bitcast (i1 (i8*)* @B.aaa to i8*)]
@.C_vtable = global [5 x i8*] [i8* bitcast (i32 (i8*)* @C.meth to i8*), i8* bitcast (i32 (i8*)* @A.bar to i8*), i8* bitcast (i32 (i8*)* @C.AAA to i8*), i8* bitcast (i1 (i8*)* @B.aaa to i8*), i8* bitcast (i32* (i8*,i1)* @C.myarrayC to i8*)]
@.AAA_vtable = global [4 x i8*] [i8* bitcast (i32 (i8*)* @A.meth to i8*), i8* bitcast (i32 (i8*)* @A.bar to i8*), i8* bitcast (i32 (i8*)* @A.AAA to i8*), i8* bitcast (i32 (i8*)* @AAA.barrrrrrr to i8*)]

declare i8* @calloc(i32, i32)
declare i32 @printf(i8*, ...)
declare void @exit(i32)

@_cint = constant [4 x i8] c "%d\0a\00"
@_cOOB = constant [15 x i8] c "Out of bounds\0a\00"
define void @print_int(i32 %i) {
	%_str = bitcast [4 x i8]* @_cint to i8*
	call i32 (i8*, ...) @printf(i8* %_str, i32 %i)
	ret void
}

define void @throw_oob() {
	%_str = bitcast [15 x i8]* @_cOOB to i8*
	call i32 (i8*, ...) @printf(i8* %_str)
	call void @exit(i32 1)
	ret void
}

define i32 @main() {
	ret i32 0
}

define i32 @A.meth(i8* %this){
	ret i32 1
}

define i32 @A.bar(i8* %this){
	ret i32 2
}

define i32 @A.AAA(i8* %this){
	ret i32 10
}

define i32 @B.meth(i8* %this){
	ret i32 4
}

define i1 @B.aaa(i8* %this){
	ret i1 1
}

define i32* @C.myarrayC(i8* %this, i1 %.p){
	%p = alloca i1
	store i1 %.p, i1* %p

	%_0 = icmp slt i32 43, 0
	br i1 %_0, label %arr_alloc0, label %arr_alloc1
arr_alloc0:
	call void @throw_oob()
	br label %arr_alloc1
arr_alloc1:
	%_1 = add i32 43, 1
	%_2 = call i8* @calloc(i32 4, i32 %_1)
	%_3 = bitcast i8* %_2 to i32*
	store i32 43, i32* %_3
	ret i32* %_3
}

define i32 @C.AAA(i8* %this){
	ret i32 423
}

define i32 @C.meth(i8* %this){
	ret i32 1
}

define i32 @AAA.barrrrrrr(i8* %this){
	%_0 = icmp slt i32 4, 0
	br i1 %_0, label %arr_alloc0, label %arr_alloc1
arr_alloc0:
	call void @throw_oob()
	br label %arr_alloc1
arr_alloc1:
	%_1 = add i32 4, 1
	%_2 = call i8* @calloc(i32 4, i32 %_1)
	%_3 = bitcast i8* %_2 to i32*
	store i32 4, i32* %_3
	%_4 = load i32, i32* %_3
	ret i32 %_4
}

