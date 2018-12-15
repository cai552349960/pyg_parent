app.controller('cartController', function ($scope, cartService) {
    $scope.findCartList = function () {
        cartService.findCartList().success(
            function (response) {
                $scope.cartList = response;
                $scope.totalValue = cartService.sum($scope.cartList);
            }
        );

    };
    $scope.addGoodsToCartList = function (itemId,num) {
        cartService.addGoodsToCartList(itemId,num).success(
            function (response) {
                if (response.success) {
                    $scope.findCartList();
                } else {
                    alter(response.message);
                }

            }
        );

    };

    $scope.selectAddress=function(address){
        $scope.address=address;
    }

    $scope.isSeletedAddress=function(address){
        if(address==$scope.address){
            return true;
        }else{
            return false;
        }
    }

    $scope.findAddressList = function () {
        cartService.findAddressList().success(
            function (response) {
                $scope.addressList = response;
                for (var i = 0; i <$scope.addressList.length ; i++) {
                    if ($scope.addressList[i].isDefined == '1') {
                        $scope.address = $scope.addressList[i];
                        break;
                    }
                }
            }
        );

    };

    $scope.order={paymentType: 1};
    $scope.selectPayType=function (type) {
        $scope.order.paymenTyp = type;
    }
    $scope.submitOrder = function () {
        $scope.order.receiverAreaName = $scope.address.address;
        $scope.order.receiverMobile = $scope.address.receiverMobile;
        $scope.order.receiver = $scope.address.contact;
        cartService.submitOrder($scope.order).success(
            function (response) {
                if (response.success) {
                    if ($scope.order.paymentType == '1') {
                        location.href = "pay.html";
                    } else {
                        location.href = "paysuccess.html";
                    }
                } else {
                    alert(response.message);
                }
            }
        )

    };
});