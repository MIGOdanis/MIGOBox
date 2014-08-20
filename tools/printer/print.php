<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>KURUMAP 列印終結者 - 目錄瀏覽器</title>
</head>
<body style="font-family:'微軟正黑體';">
<h1>KURUMAP 列印終結者 - 目錄瀏覽器</h1><a href="底紙.docx">下載底紙</a>
分頁符號取代 :  ^n
<?
//設置目錄
$dir = (isset($_GET['dir']))?$_GET['dir']:"/";
//取得目錄下的資料夾(與檔案) 此版本還沒解決分辨資料夾與檔案的功能
$array = scandir(".".mb_convert_encoding($dir, "big5", "UTF-8"));
echo "<h3>目錄".".".$dir."</h3>";
foreach($array as $row){
	$file = "./".$_GET['dir']."/".$row;
	$row = iconv("BIG5", "UTF-8",$row);
	//回根目錄
	if($row=="."){
		echo "<a href='print.php?dir=' ><strong>.</strong></a><br>";	
	//回上一層
	}elseif($row==".."){
		//此處先將目錄拆成陣列彈掉最後一個陣列後重新組裝
		$str = explode("/", $_GET['dir']);
		$n = count($str)-1;
		unset($str[$n]);
		$str = implode ("/", $str);
		echo "<a href='print.php?dir=".$str."' ><strong>..</strong></a><br>";	
	}else{
		$filetype = array_pop(explode('.',$file));
		$typarray = array("php","js","css");
		if(is_dir(urldecode($file))){
			echo "<a href='print.php?dir=".$_GET['dir']."/".$row."' ><strong>".$row."</strong></a><br>";
		}else{
			if(in_array($filetype,$typarray)){
				echo "<strong>".$row."</strong>( <a target='new' href='print_all.php?dir=".$file."'>單獨開啟</a> )<br>";
			}else{
				echo "<strong>".$row."</strong><br>";
			}
		}
	}
}

echo "<br /><a target='new' href='print_all.php?dir=".$_GET['dir']."' >>>><strong>在此目錄輸出原始碼</strong><<<</a>"
?>
</body>
</html>
