
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

				var value = $('#value_id').val();
				if(value==''||value==null){
					$.messager.alert('提示', '请输入转账ETH金额', 'info');
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

