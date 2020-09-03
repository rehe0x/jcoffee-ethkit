
$(document).ready(function(){

	 $('#window').window('close');
	 $('#window2').window('close');
	 $('#window3').window('close');
	 $('#window4').window('close');
	 $('#window5').window('close');

	 getContractList();
	 gasPrice(1);
	 $("#refresh11").click(function(){
		 var contractAddress = $('#coin_1').combobox('getValue');
		 loadAddrs("master","t11",contractAddress,1);
	 });

	 $("#refresh12").click(function(){
		 var contractAddress = $('#coin_2').combobox('getValue');
		 loadAddrs("slave","t12",contractAddress,1);
	 });

	$("#pubKey").click(function(){
		getPubKey();
	});
	
	$("#balance_btn").click(function(){
		getErc20Balance();
	});

	$("#gasprice_btn").click(function(){
		gasPrice(2);
	});

	$("#move11").click(function(){
		moveWallet(2);
	});
	
	$("#move12").click(function(){
		 moveWallet(1);
	});

	$("#add_coin1").click(function(){
		$("#window5").window('open');
		$('#contract_add_id').textbox("setValue", "");
	});

	$("#add_coin2").click(function(){
		$("#window5").window('open');
		$('#contract_add_id').textbox("setValue", "");
	});

	
	$("#select1").click(function(){
		var start = $('#select1_start').val();
		var end = $('#select1_end').val();
		start = parseInt(start);
		end = parseInt(end);
		start = start-1;
		$('#t11').datagrid('clearSelections');
		for(var i=start;i<end;i++){
			$('#t11').datagrid("selectRow", i);
		}
	});

	$("#select2").click(function(){
		var start = $('#select2_start').val();
		var end = $('#select2_end').val();
		start = parseInt(start);
		end = parseInt(end);
		start = start-1;
		$('#t12').datagrid('clearSelections');
		for(var i=start;i<end;i++){
			$('#t12').datagrid("selectRow", i);
		}
	});

	
	$('input:radio').click(function () {
		var transType = $("input[name='transType']:checked").val();
		if(transType==3){
			$('#address_tr').show();
			$('#contract_tr').show();
			$('#value_tr').hide();
			$('#value2_tr').show();

		}else if(transType==1){
			$('#address_tr').hide();
			$('#contract_tr').hide();
			$('#value_tr').show();
			$('#value2_tr').hide();
		}
	});

	$('input:radio').click(function () {
		var createType = $("input[name='createType']:checked").val();
		if(createType==1){
			$('#count_tr').show();
			$('#pwd_tr').show();
			$('#repassword_tr').show();
			$('#keystore_tr').hide();
			$('#privatekey_tr').hide();
			$('#privatekey_desc').hide();
		}else if(createType==2){
			$('#count_tr').hide();
			$('#pwd_tr').show();
			$('#repassword_tr').hide();
			$('#keystore_tr').show();
			$('#privatekey_tr').hide();
			$('#privatekey_desc').hide();
		}
		else if(createType==3){
			$('#count_tr').hide();
			$('#pwd_tr').hide();
			$('#repassword_tr').hide();
			$('#keystore_tr').hide();
			$('#privatekey_tr').show();
			$('#privatekey_desc').show();
		}
	});

	$('#numType_id').switchbutton({
		checked: false,
		onChange: function(checked){
			if (checked){
				$('#value2_id').next().hide();
				$('#num_type_id').val(2);
			}else{
				$('#value2_id').next().show();
				$('#num_type_id').val(1);
			}
		}
	});

	$('#coin_1').combobox({
          onChange: function (newValue, oldValue) {
        	  loadAddrs("master","t11",newValue,1);
          }  
      });  
	  $('#coin_2').combobox({  
          onChange: function (newValue, oldValue) {
        	  loadAddrs("slave","t12",newValue,1);
          }  
      });

	$('#form1').form({
		url:'/transaction/toMany',
		onSubmit:function(){
			var validateFlag = $(this).form('validate');
			if(!validateFlag){
				$.messager.alert('提示', '表单验证未通过，请检查是否有必填项未填写', 'info');
				return false;
			}
			var address1Arr = [];
			var address2Arr = [];
			var rows = $('#t11').datagrid('getSelections');
			for(var i=0; i<rows.length; i++){
				address1Arr.push(rows[i].address);
			}

			var address1 = address1Arr.join(',');
			//console.log(address1);

			var rows2 = $('#t12').datagrid('getSelections');
			for(var i=0; i<rows2.length; i++){
				address2Arr.push(rows2[i].address);
			}

			var address2 = address2Arr.join(',');
			//console.log(address2);
			var address = $('#address_id').val();
			var transType = $("input[name='transType']:checked").val();
			
			if(transType==3){
				if(address1Arr.length==0&&address2Arr.length==0){
					$.messager.alert('提示', '请至少选择一个钱包', 'info');
					return false;
				}

				var address = $('#address_id').val();
				if(address==''||address==null){
					$.messager.alert('提示', '请输入收款钱包地址', 'info');
					return false;
				}

                if($('#num_type_id').val()==1){
					var value2 = $('#value2_id').val();
					if(value2==''||value2==null){
						$.messager.alert('提示', '请输入转账金额', 'info');
						return false;
					}
				}

				var fromAddrsLen = address1Arr.length+address2Arr.length;
				var addressArr = address.split(",");
				var addressArrLen = addressArr.length;
				if(addressArrLen>1&&fromAddrsLen>1&&fromAddrsLen!=addressArrLen){
					$.messager.alert('提示', '多对多转账需要选择与收款地址相同数量的转出钱包', 'info');
					return false;
				}

				var r=confirm("请仔细核对收款地址【"+address+"】和转账金额,确定要转账吗？");
				if (!r){
					return false;
				}

			}else if(transType==1){
				if(address1Arr.length==0){
					$.messager.alert('提示', '请选择主钱包', 'info');
					return false;
				}
				if(address2Arr.length==0){
					$.messager.alert('提示', '选择次钱包', 'info');
					return false;
				}
				if(!(address1Arr.length==1||address1Arr.length==address2Arr.length)){
					$.messager.alert('提示', '请选择一个主钱包或选择相同数量的主钱包和次钱包', 'info');
					return false;
				}
				var value = $('#value_id').val();
				if(value==''||value==null){
					$.messager.alert('提示', '请输入转账ETH金额', 'info');
					return false;
				}

				var r=confirm("请仔细核转账金额"+value+",确定要转账吗？");
				if (!r){
					return false;
				}
			}
			$('#from_id1').val(address1);
			$('#to_id1').val(address2);
			ajaxLoading();
		},
		success:function(data){
			//$('#address_id').textbox("setValue", "");
			ajaxLoadEnd();
			var obj = JSON.parse(data);
			if(obj.success){
				//console.log(obj.data);
				var transType = $("input[name='transType']:checked").val();
				$('#window3').window('open');
				$('#window3_div').text(obj.message);
				$('#window3_table').datagrid('loadData', obj.data);
				var address = $('#address_id').val();
				$('#address_id').textbox("setValue", "");
				//$.messager.alert('提示', '已经将之前填写的收款地址【'+address+'】清空,防止下载转账忘记修改地址', 'info');
			}else{
				$.messager.alert('提示', obj.message, 'info');
			}

		}
	});

	$('#form2').form({
		url:'/transaction/createWallet',
		onSubmit:function(){

			var validateFlag = $(this).form('validate');
			if(!validateFlag){
				$.messager.alert('提示', '表单填写不合法，请检查', 'info');
				return false;
			}

			var createType = $("input[name='createType']:checked").val();
			if(createType==1){
				var count = $("#count_id").val();
				if(count==''||count==null){
					$.messager.alert('提示', '请输入创建数量', 'info');
					return false;
				}
				var pwd_id = $("#pwd_id").val();
				var repassword = $("#repassword").val();
				if(pwd_id==''||pwd_id==null){
					$.messager.alert('提示', '请输入钱包密码', 'info');
					return false;
				}
				if(repassword==''||repassword==null){
					$.messager.alert('提示', '请输入确认密码', 'info');
					return false;
				}
				if(pwd_id!=repassword){
					$.messager.alert('提示', '两次输入密码不匹配', 'info');
					return false;
				}
			}else if(createType==2){
				var pwd_id = $("#pwd_id").val();
				if(pwd_id==''||pwd_id==null){
					$.messager.alert('提示', '请输入钱包密码', 'info');
					return false;
				}
				var keystore = $("#keystore_id").val();
				if(keystore==''||keystore==null){
					$.messager.alert('提示', '请输入keystore文本', 'info');
					return false;
				}
			}else if(createType==3){
				var privatekey_id = $("#privatekey_id").val();
				if(privatekey_id==''||privatekey_id==null){
					$.messager.alert('提示', '请输入地址私钥对', 'info');
					return false;
				}
			}
			var r=confirm("确定要提交吗？");
			if (!r){
				return false;
			}
			ajaxLoading();
			return validateFlag;
		},
		success:function(data){
			ajaxLoadEnd();
			var obj = JSON.parse(data);
			if(obj.success){
				//console.log(obj.data);
				$('#window2').window('open');
				$('#window2_div').text(obj.message);
				$('#window2_table').datagrid('loadData', obj.data);
				
			}else{
				$.messager.alert('提示', obj.message, 'info');
			/*	$('#window2').window('open');
				$('#window2_div').text(obj.message);
				$('#window2_table').datagrid('loadData', {});*/
			}

		}
	});

	$('#form5').form({
		url:'/transaction/addContract',
		onSubmit:function(){
			$('#window5').window('open');
			var validateFlag = $(this).form('validate');
			if(!validateFlag){
				$.messager.alert('提示', '表单填写不合法，请检查', 'info');
				return false;
			}

			//ajaxLoading();
			$.messager.progress({
				title:'Please waiting',
				msg:'正在处理中...'
			});
			return validateFlag;
		},
		success:function(data){
			//ajaxLoadEnd();
			$.messager.progress("close");
			var obj = JSON.parse(data);
			if(obj.success){
				$("#window5").window('close');
				$('#contract_add_id').textbox("setValue", "");
				$.messager.alert('提示', obj.message, 'info');
				getContractList();
			}else{
				$.messager.alert('提示', obj.message, 'info');
			}

		}
	});
	
});

var loadFlag1 = false;
var loadFlag2 = false;

function firstLoad(){
	ajaxLoading();
	loadAddrs("master","t11","ETH",1);
    loadAddrs("slave","t12","ETH",1);
}

function ajaxLoading(){
	$("<div class=\"datagrid-mask\"></div>").css({display:"block",width:"100%",height:$(window).height()}).appendTo("body");
	$("<div class=\"datagrid-mask-msg\"></div>").html("正在处理中，请稍候。。。").appendTo("body").css({display:"block",left:($(document.body).outerWidth(true) - 190) / 2,top:($(window).height() - 45) / 2});
}
function ajaxLoadEnd(){
	$(".datagrid-mask").remove();
	$(".datagrid-mask-msg").remove();
}

var masterSum =0;
var slaveSum = 0;
var decimals = 10;
function loadAddrs(dir,tableId,coin,isSearchBalance){
		decimals = 10;
		if(coin==undefined){
			coin ="ETH";
		}
		if(coin!="ETH"){
			decimals = parseInt(coin.split("_")[1]);
		}
		console.log("decimals:"+decimals);
		if(loadFlag1==true&&loadFlag2==true){
			ajaxLoading();
		}
		$.ajax({
			type: "POST",
			url: "/transaction/getAddressList",
			timeout : 300000,
			async: true,
			data: {"dir":dir,"coin":coin,"isSearchBalance":0},
			//返回数据的格式
			dataType: "json",
			success: function (rs){
				if(dir=='master'){
					loadFlag1 = true;
				}
				if(dir=='slave'){
					loadFlag2 = true;
				}
				if(loadFlag1==true&&loadFlag2==true){
					ajaxLoadEnd();
				}
				
				if(rs.success==true){
					var list = rs.data.list;
					$('#'+tableId).datagrid('loadData', list);
					if(dir=="master"){
						 masterSum =0;
						$('#balance1_id').textbox("setValue", rs.data.sum);
					}else{
						 slaveSum = 0;
						$('#balance2_id').textbox("setValue", rs.data.sum);
					}
					
					var rows = $('#'+tableId).datagrid('getRows');
					var address;
					for(var i=0; i<rows.length; i++){
						rows[i].balance = "正在查询...";
						$('#'+tableId).datagrid('refreshRow',i);
						loadBalance(coin,dir,tableId,rows[i],i);
					}
					
				}else{
					$.messager.alert('提示', rs.message, 'info');
				}
			},
			error:function(){
				$.messager.alert('提示',"程序可能被停止了，请检查！", 'info');
			}
			,complete: function (XMLHttpRequest, status) { //请求完成后最终执行参数
				
				if (status == 'timeout') {//超时,status还有success,error等值的情况
					$.messager.alert('提示', "请求超时", 'info');
				}
			 }
		});
}

function loadBalance(coin,dir,tableId,row,rowIndex){
	$.ajax({
		type: "POST",
		url: "/transaction/getAddressList",
		timeout : 1000000,
		async: true,
		data: {"dir":row['address'],"coin":coin,"isSearchBalance":1},
		//返回数据的格式
		dataType: "json",
		success: function (rs){
			if(rs.success==true){
				var list = rs.data.list;
				var rsLenght = list.length;
				var item;
				for(var i= 0;i<rsLenght;i++){
					item = list[i];
					//console.log(item);
					//$("."+item.address).val(item.balance);
					//$("."+item.address).text(item.balance);
					//$("."+item.address).textbox("setValue", item.balance);
					
					row.balance = item.balance;
					$('#'+tableId).datagrid('refreshRow',rowIndex);
					
					if(dir=="master"){
						masterSum = masterSum+parseFloat(item.balance);
						masterSum = masterSum.toFixed(decimals);
						masterSum = parseFloat(masterSum);
						$('#balance1_id').textbox("setValue", masterSum);
					}else{
						slaveSum = slaveSum+parseFloat(item.balance);
						slaveSum = slaveSum.toFixed(decimals);
						slaveSum = parseFloat(slaveSum);
						$('#balance2_id').textbox("setValue", slaveSum);
					}
				}
			}
			else{
				//$.messager.alert('提示', rs.message, 'info');
				console.log(rs.message);
			}
		},
		error:function(){
			// $.messager.alert('提示',"程序可能被停止了，请检查！", 'info');
			console.log("查询余额失败，请刷新");
			row.balance = "查询余额失败，请刷新";
			$('#'+tableId).datagrid('refreshRow',rowIndex);
		}
		,complete: function (XMLHttpRequest, status) {
			 if (status == 'timeout') {
				 //$.messager.alert('提示', "请求超时", 'info');
				 console.log("程序可能被停止了，请检查！");
			 }
		 }
	});
}

function getPubKey(){
	ajaxLoading();
    $.ajax({
        type: "get",
        url: "/transaction/getPubKey?random="+Math.random(),
        async: true,
        data: {},
        //返回数据的格式
        dataType: "json",
        success: function (data){
        	ajaxLoadEnd();
        	//$('#pwd_id').passwordbox("setValue", "");
			$('#window4').window('open');
			$('#window4_div').text(data.message);
        },
		error:function(){
			$.messager.alert('提示',"程序可能被停止了，请检查！", 'info');
		},
        complete: function (XMLHttpRequest, status) { //请求完成后最终执行参数
        	ajaxLoadEnd();
            if (status == 'timeout') {//超时,status还有success,error等值的情况
				$.messager.alert('提示', "请求超时", 'info');
            }
        }
    });
}

function getContractList(){
	ajaxLoading();
    $.ajax({
        type: "get",
        url: "/transaction/getContractList?random="+Math.random(),
        async: true,
        data: {},
        //返回数据的格式
        dataType: "json",
        success: function (data){
        	ajaxLoadEnd();
        	//console.log(data);
			$('#coin_1').combobox({
				valueField:'address',
				textField:'symbol',
				data:data
			});
			$('#coin_2').combobox({
				valueField:'address',
				textField:'symbol',
				data:data
			});
			$('#coin_3').combobox({
				valueField:'address',
				textField:'symbol',
				data:data
			});
        },
		error:function(){
			$.messager.alert('提示',"程序可能被停止了，请检查！", 'info');
		},
        complete: function (XMLHttpRequest, status) { //请求完成后最终执行参数
        	ajaxLoadEnd();
            if (status == 'timeout') {//超时,status还有success,error等值的情况
				$.messager.alert('提示', "请求超时", 'info');
            }
        }
    });
}

function getErc20Balance(){
	
	var address1Arr = [];
	var address2Arr = [];
	var rows = $('#t11').datagrid('getSelections');
	for(var i=0; i<rows.length; i++){
		address1Arr.push(rows[i].address);
	}

	var address1 = address1Arr.join(',');

	var rows2 = $('#t12').datagrid('getSelections');
	for(var i=0; i<rows2.length; i++){
		address2Arr.push(rows2[i].address);
	}

	var address2 = address2Arr.join(',');
	
	if(address1Arr.length==0&&address2Arr.length==0){
		$.messager.alert('提示', '请至少选择一个钱包', 'info');
		return false;
	}

	var contractAddress = $('#coin_3').combobox('getValue');

	var address = address1+","+address2;
	
	ajaxLoading();
    $.ajax({
        type: "post",
        url: "/transaction/getErc20Balance",
        timeout : 300000,
        async: true,
        data: {"address":address,"contractAddress":contractAddress},
        //返回数据的格式
        dataType: "json",
        success: function (res){
        	ajaxLoadEnd();
        	//alert(res.data);
        	if(res.success){
        		$('#balance_id').textbox("setValue", res.data);
        	}else{
        		$.messager.alert('提示',res.message, 'info');
        		$('#balance_id').textbox("setValue", "");
        	}
        	
        },
		error:function(){
			$.messager.alert('提示',"程序可能被停止了，请检查！", 'info');
		},
        complete: function (XMLHttpRequest, status) { //请求完成后最终执行参数
        	ajaxLoadEnd();
            if (status == 'timeout') {//超时,status还有success,error等值的情况
				$.messager.alert('提示', "请求超时", 'info');
            }
        }
    });
}

function gasPrice(status){
	ajaxLoading();
	$.ajax({
		type: "get",
		url: "/transaction/gasPrice",
		async: true,
		data: {},
		//返回数据的格式
		dataType: "json",
		success: function (res){
			ajaxLoadEnd();
			if(res.success){
				$('#gasprice_id').textbox("setValue", res.data);
				if(status==1){
					$('#gasprice_item_id').textbox("setValue", res.data);
				}
			}
		},
		error:function(){
			$.messager.alert('提示',"程序可能被停止了，请检查！", 'info');
		},
		complete: function (XMLHttpRequest, status) { //请求完成后最终执行参数
			ajaxLoadEnd();
			if (status == 'timeout') {//超时,status还有success,error等值的情况
				$.messager.alert('提示', "请求超时", 'info');
			}
		}
	});
}



function moveWallet(walletType){
	
	var addressArr = [];
	var rows;
	
	if(walletType==1){
		rows = $('#t12').datagrid('getSelections');
	}else{
		rows = $('#t11').datagrid('getSelections');
	}
	
	for(var i=0; i<rows.length; i++){
		addressArr.push(rows[i].address);
	}
	
	if(addressArr.length==0&&addressArr.length==0){
		$.messager.alert('提示', '请选择要移动的钱包', 'info');
		return false;
	}
	
	var address = addressArr.join(',');
	
	ajaxLoading();
	
    $.ajax({
        type: "post",
        url: "/transaction/moveWallet",
        timeout : 300000,
        async: true,
        data: {"address":address,"walletType":walletType},
        //返回数据的格式
        dataType: "json",
        success: function (res){
        	ajaxLoadEnd();
        	loadAddrs("master","t11");
    		loadAddrs("slave","t12");
        },
        error:function(){
			$.messager.alert('提示',"程序可能被停止了，请检查！", 'info');
		},
        complete: function (XMLHttpRequest, status) { //请求完成后最终执行参数
        	ajaxLoadEnd();
            if (status == 'timeout') {//超时,status还有success,error等值的情况
				$.messager.alert('提示', "请求超时", 'info');
            }
        }
    });
}