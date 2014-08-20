<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>KURUMAP 列印終結者 - 檔案連結器</title>
</head>
<body>
<a href="print_all.php?dir=<?php echo $_GET['dir']?>&type=php">輸出PHP</a> | 
<a href="print_all.php?dir=<?php echo $_GET['dir']?>&type=js">輸出JavaSript</a> | 
<a href="print_all.php?dir=<?php echo $_GET['dir']?>&type=css">輸出CSS</a>
<div style='font-size:14px;'>
<?php
// 設定資料夾位置與副檔名
if(is_dir(".".$_GET['dir'])){
	$type = (isset($_GET['type']))? $_GET['type'] : "php";
	// 將所有符合目標製成陣列
	$dir = glob(".".$_GET['dir']."/*.".$type);
}else{
	$dir = glob($_GET['dir']);
}
echo "<strong>目錄 .".$_GET['dir']."/*.php (共 ".count($dir)." 件)</strong><br>";
// 開始輸出
foreach($dir as $row){
	echo "<strong>".$row."</strong><br>";
	$file = fopen($row, "r");
	while(! feof($file))
	{
		// 防止html被隨之echo的作法 並且將\t取代為 /t/避免被遮蔽
		$a = htmlspecialchars(str_replace( "\t" , "/t/" , fgets($file)))."<br>";
		// 還原TAB排版
		$a = str_replace( "/t/" , "&nbsp;&nbsp;&nbsp;&nbsp;" ,$a);
		// 還原空白排版
		echo  str_replace( " " , "&nbsp;" ,$a);
	}
	fclose($file);
	
	//製作分頁點
	echo "//分頁符號//<br />";
}
?>
</div>
</body>
</html>
