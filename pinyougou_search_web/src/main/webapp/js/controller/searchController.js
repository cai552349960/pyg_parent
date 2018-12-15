app.controller('searchController',function($location,$scope,searchService){
    $scope.searchMap={'keywords':'','category':'','brand':'','spec':{},'price':'',
            'pageNo':1,'pageSize':40,'sortField':'','sort':''};
	//搜索
	$scope.search=function(){
        $scope.searchMap.pageNo= parseInt($scope.searchMap.pageNo) ;
		searchService.search($scope.searchMap).success(
			function(response){
				$scope.resultMap=response;
                buildPageLabel();//调用
			}
		);		
	}


	$scope.addSearchItem=function (key, value) {
        if (key == 'category' || key == 'brand' || key == 'price') {
            $scope.searchMap[key] = value;
        } else {
            $scope.searchMap.spec[key] = value;
        }
        $scope.search();
    }

    $scope.removeSearchItem=function (key) {
        if (key == "category" || key == "brand" || key == 'price')  {
            $scope.searchMap[key] = "";
        } else {
            delete $scope.searchMap.spec[key];
        }
        $scope.search();
    }
    buildPageLabel=function(){
        $scope.pageLabel=[];//新增分页栏属性
        var maxPageNo= $scope.resultMap.totalPages;//得到最后页码
        var firstPage=1;//开始页码
        var lastPage=maxPageNo;//截止页码
        $scope.firstDot=false;//前面有点
        $scope.lastDot=false;//后边有点
        if($scope.resultMap.totalPages> 5){  //如果总页数大于5页,显示部分页码
            if($scope.searchMap.pageNo<=3){//如果当前页小于等于3
                lastPage=5; //前5页
                $scope.lastDot=true;
            }else if( $scope.searchMap.pageNo>=lastPage-2  ){//如果当前页大于等于最大页码-2
                firstPage= maxPageNo-4;		 //后5页
                $scope.firstDot=true;
            }else{ //显示当前页为中心的5页
                firstPage=$scope.searchMap.pageNo-2;
                lastPage=$scope.searchMap.pageNo+2;
                $scope.firstDot=true;//前面无点
                $scope.lastDot=true;
            }
        }
        //循环产生页码标签
        for(var i=firstPage;i<=lastPage;i++){
            $scope.pageLabel.push(i);
        }
    }
    $scope.queryByPage=function(pageNo){
        //页码验证
        if(pageNo<1 || pageNo>$scope.resultMap.totalPages){
            return;
        }
        $scope.searchMap.pageNo=pageNo;
        $scope.search();
    }
    //判断当前页为第一页
    $scope.isTopPage=function(){
        if($scope.searchMap.pageNo==1){
            return true;
        }else{
            return false;
        }
    }

    //判断当前页是否未最后一页
    $scope.isEndPage=function(){
        if($scope.searchMap.pageNo==$scope.resultMap.totalPages){
            return true;
        }else{
            return false;
        }
    }
    $scope.sortSearch=function (sortField, sort) {
        $scope.searchMap.sortField = sortField;
        $scope.searchMap.sort = sort;
        $scope.search();
    }
    $scope.keywordsIsBrand = function () {
	    for (var i=0;i<$scope.resultMap.brandList.length;i++){
            if ($scope.searchMap.keywords.indexOf($scope.resultMap.brandList[i].text )>= 0) {
                return true;
            }
        }
        return false;
    };
    $scope.loadkeywords = function () {
        $scope.searchMap.keywords = $location.search()['keywords'];
        $scope.search();

    };
});