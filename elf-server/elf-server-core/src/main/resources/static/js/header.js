$(document).ready(function () {
    var headers = [{
        name: "首页",
        href: "/",
        active: function () {
            if (window.location.pathname == "/" || window.location.pathname == "/qconsole.html") {
                return true;
            }
            return false;
        }
    }, {
        name: "主机信息",
        href: "/machine.html",
        active: function () {
            if (window.location.pathname == "/machine.html") {
                return true;
            }
            return false;
        }
    }, {
        name: "在线Debug",
        href: "/debug.html",
        active: function () {
            if (window.location.pathname == "/debug.html") {
                return true;
            }
            return false;
        }
    }, {
        name: "动态监控",
        href: "/monitor.html",
        active: function () {
            if (window.location.pathname == "/monitor.html") {
                return true;
            }
            return false;
        }
    }, {
        name: "文件下载",
        href: "/download.html",
        active: function () {
            if (window.location.pathname == "/download.html") {
                return true;
            }
            return false;
        }
    }, {
        name: "应用中心",
        href: "/application.html",
        active: function () {
            if (window.location.pathname == "/application.html") {
                return true;
            }
            return false;
        }
    }]

    function initHeader() {
        var href = window.location.href;
        var containerFluid = $("<div></div>").addClass("container-fluid");
        var navbarHeader = $("<div></div>").addClass("navbar-header").append($("<a></a>").attr("href", "/").append($("<img>").attr("src", "/image/tcdev.png")));

        var navbarLeft = $("<ul></ul>").addClass("nav navbar-nav navbar-left");
        headers.forEach(function (header) {
            var li = $("<li></li>").append($("<a></a>").attr("href", header.href).append(header.name));
            if (typeof header.active == "function" && header.active.call()) {
                li.addClass("active");
            }
            navbarLeft.append(li);
        })

        var navbarRight = $("<ul></ul>").addClass("nav navbar-nav navbar-right");
        var back = $("<li></li>").append($("<a></a>").addClass("navbar-brand back").attr("href", "/").append("返回首页"));
        var logout = $("<li></li>").append($("<a></a>").addClass("navbar-brand back").attr("href", "/logout.do").append("退出"));
        // TODO
        // if (href.indexOf("help.html") < 0) {
        //     var help = $("<li></li>").append($("<a></a>").addClass("navbar-brand").attr("href", "/help.html").append("帮助文档"));
        //     navbarRight.append(help)
        // }
        navbarRight.append(back);
        // TODO
        // navbarRight.append(logout);

        var navbarCollapse = $("<div></div>").addClass("collapse navbar-collapse").attr("id", "bs-example-navbar-collapse-1");
        navbarCollapse.append(navbarLeft).append(navbarRight);

        containerFluid.append(navbarCollapse);
        var nav = $("<nav></nav>").addClass("navbar navbar-default");
        nav.append(containerFluid);
        $("#header").append(nav);
    }

    window.appcenter = "";

    initHeader();
})