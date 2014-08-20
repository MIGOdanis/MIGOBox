<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<h1> 小寫轉大寫 </h1> BY MIGO Danis
<form id="site-setting-form" action="" method="post">
	<div>
		<textarea rows="40" cols="180" name="text" id="text"><?php
				if(isset($_POST)){
					echo strtoupper($_POST['text']);
				}
			?></textarea>
	</div>
	<input type="submit" name="yt0" value="轉換">
</form>
