"use strict";

String.prototype.capitalize = function () {
    return this.charAt(0).toUpperCase() + this.slice(1);
};

var numberWithCommas = function numberWithCommas(x) {
    return x.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",");
};

var currentMonth = function currentMonth() {
    var mL = ['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December'];
    var date = new Date();
    return mL[date.getMonth()];
};

var app = {

    goals: [],
    showCurrentGoals: false,
    justSavedAmount: 0,

    goalType: "not defined",
    groupType: "not defined",
    selectedMonth: "",
    contributions: "",
    isValid: false,
    maxAmount: 0,
    totalContributions: 0,
    balance: 0,
    monthDays: 0,
    remainingDays: 0,
    groupStats: {
        groupTotal: 0,
        groupMax: 0,
        groupRem: 0,
        goalReachers: 0,
        groupSize: 0
    },

    dataStore: [],
    populateDs: function populateDs(item) {
        this.dataStore.push(item);
    },
    surveyId: null,
    appStartTimeStamp: null,
    currentPageTimestamp: null,
    pageOrder: 1,
    prevPage: "index",
    timeTracker: function timeTracker(curTimeStamp) {
        var floorTimeStamp = this.currentPageTimestamp;
        this.currentPageTimestamp = curTimeStamp;
        return moment.duration(moment(curTimeStamp).diff(moment(floorTimeStamp)))._milliseconds;
    },
    util: {
        getPageTitleFromUrl: function getPageTitleFromUrl(absUrl) {
            var pageIdMatch = /\/(\w+)\./;
            return pageIdMatch.exec(absUrl)[1];
        }

    },

    initBindings: function () {
        rivets.bind(document.body, {
            goals: app.goals,
            showCurrentGoals: app.showCurrentGoals,
            controllers: {
                navigateToGoal: function(e, goal) {
                    var goal = app.goals[goal.index];
                    window._es.setCurrentGoalId(function() {

                        app.updateSessionVars();

                        $.mobile.changePage('group_stats.html');
                    }, function() {}, goal.id);
                }
            }
        });
    },

    getMiniMonth: function (month) {
        var mm = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'June', 'July', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];

        return mm[month];
    },

    getWeekString: function () {
        var curr = new Date();
        var first = moment(curr).subtract(curr.getDay(), 'd');
        var last = moment(first).add(6, 'd');

        var week = first.format('Do MMM') + " - " + last.format('Do MMM');

        return week;
    },

    updateSessionVars: function () {
        var currentPage = arguments[0];

        window._es.getSessionDetails(function (session) {
            app.surveyId = session.phone;
            app.goalType = session.goalType;
            app.groupType = session.groupType;
            app.selectedMonth = session.month;
            app.contributions = session.contributions;
            app.isValid = session.isValid;
            app.maxAmount = session.maxAmount;
            app.totalContributions = session.totalContributions;
            app.balance = session.balance;
            app.remainingDays = session.remainingDays;
            app.monthDays = session.monthDays;

            if (currentPage === 'group_stats')
                app.updateMyStats();

        }, function (error) {
            console.error(error);
        }, "");

        window._es.getGroupStatistics(function (stats) {
            console.log("GROUP_STATS=", stats);
            app.groupStats = stats;
            if (currentPage === 'group_stats')
                app.updateStats();
        }, function () {
            console.log("Error")
        });
    },

    initialize: function initialize() {
        app.bindEvents();
    },


    bindEvents: function bindEvents() {

        console.log("Binding events");

        document.addEventListener('deviceready', app.onDeviceReady, false);
    },

    updateMyStats: function() {
        var goalType = app.goalType;
        var group = app.groupType;
        var amount = 0;

        var img = 'img/default-image.jpg';
        if (group === 'savvy') {
            img = 'img/icon-money-bag-smallest.png';
            amount = 500;
        } else if (group === 'power') {
            img = 'img/icon-money-bag-small.png';
            amount = 1000;
        } else if (group === 'super') {
            img = 'img/icon-money-bag-big.png';
            amount = 2000;
        } else if (group === 'champion') {
            img = 'img/icon-money-bag-biggest.png';
            amount = 2000;
        }
        $("#gs-img").attr('src', img);
        $(".gs-goal").html(goalType.capitalize());
        $(".gs-group").html(group.capitalize());
        $("[data-role=rem-days]").html(app.remainingDays);
        $("#my-amount-saved").html(app.totalContributions);
        $("#amount-balance").html(app.balance);

        // Draw charts
        var percentageSaved = Math.round(app.totalContributions / app.maxAmount * 100);
        var percentageRem = 100 - percentageSaved;

        Highcharts.setOptions({
            colors: ['#00b200', '#F5F5F5']
        });

        Highcharts.chart('my-pie-progress', {
            chart: {
                plotBackgroundColor: null,
                plotBorderWidth: null,
                plotShadow: false,
                type: 'pie',
                margin: 0
            },
            title: {
                text: "<p style='text-align: center;margin: 0;'><span class='chart-percent'>" + percentageSaved + "%</span>" +
                "<span class='chart-saved-text'>Saved</span></p>",
                verticalAlign: 'middle',
                useHTML: true,
                margin: [0, 0, 0, 0]
            },
            credits: {enabled: false},
            tooltip: {
                enabled: false
            },
            plotOptions: {
                pie: {
                    allowPointSelect: false,
                    cursor: 'pointer',
                    dataLabels: {
                        enabled: false
                    },
                    showInLegend: false,
                    enableMouseTracking: false,
                }
            },
            series: [{
                name: 'Brands',
                colorByPoint: true,
                innerSize: '75%',
                data: [
                    ['Saved', percentageSaved],
                    ['Remaining', percentageRem]
                ]
            }],
            legend: {
                enabled: false
            }
        });
    },

    updateStats: function () {

        console.log("UPDATING STATS");

        $("#groupCount").html(app.groupStats.groupSize);
        $("#groupRem").html(app.groupStats.groupRem);
        $("#goalReachers").html(app.groupStats.goalReachers);
        $("#groupAmount").html(app.groupStats.groupTotal);
        $("#member-count").html(app.groupStats.groupSize);

        var groupPercentSaved = 0;
        if (app.groupStats.groupMax !== 0)
            groupPercentSaved = Math.round(app.groupStats.groupTotal / app.groupStats.groupMax * 100);

        var groupPercentageRem = 100 - groupPercentSaved;

        Highcharts.setOptions({
            colors: ['#FFA500', '#F5F5F5']
        });

        Highcharts.chart('group-pie-progress', {
            chart: {
                plotBackgroundColor: null,
                plotBorderWidth: null,
                plotShadow: false,
                type: 'pie',
                margin: 0
            },
            title: {
                text: "<p style='text-align: center;margin: 0;'><span class='chart-percent'>" + groupPercentSaved + "%</span>" +
                "<span class='chart-saved-text'>Saved</span></p>",
                verticalAlign: 'middle',
                useHTML: true,
                margin: [0, 0, 0, 0]
            },
            credits: {enabled: false},
            tooltip: {
                enabled: false
            },
            plotOptions: {
                pie: {
                    allowPointSelect: false,
                    cursor: 'pointer',
                    dataLabels: {
                        enabled: false
                    },
                    showInLegend: false,
                    enableMouseTracking: false,
                }
            },
            series: [{
                name: 'Brands',
                colorByPoint: true,
                innerSize: '75%',
                data: [
                    ['Saved', groupPercentSaved],
                    ['Remaining', groupPercentageRem]
                ]
            }],
            legend: {
                enabled: false
            }
        });
    },

    onDeviceReady: function onDeviceReady() {

        window._es.getGroupStatistics(function (stats) {
            console.log("GROUP_STATS=", stats);
            app.groupStats = stats;
        }, function () {
            console.log("Error")
        });

        window._es.getSessionDetails(function (session) {
            app.surveyId = session.phone;
            app.goalType = session.goalType;
            app.groupType = session.groupType;
            app.selectedMonth = session.month;
            app.contributions = session.contributions;
            app.isValid = session.isValid;
            app.maxAmount = session.maxAmount;
            app.totalContributions = session.totalContributions;
            app.balance = session.balance;
            app.remainingDays = session.remainingDays;
            app.monthDays = session.monthDays;

            if (app.isValid) {

                setTimeout(function () {
                    $.mobile.changePage('homepage.html');
                }, 100)

            } else {
                setTimeout(function () {
                    $.mobile.changePage('login.html');
                }, 100);
            }

        }, function (error) {
            console.error(error);
            setTimeout(function () {
                $.mobile.changePage('login.html');
            }, 100);
        }, "");

        $(document).on('click', '[data-role="goal_type_selection"]', function (e) {

            var target = $(e.target);
            var anchor = target.closest('a');
            var goalType = anchor.attr('data-goal-type');
            if (!goalType) return;
            var params = {goalType: goalType};
            app.goalType = goalType;
            if (goalType) {
                window._es.saveGoalType(function () {
                    console.log("Saved successfully")
                }, function () {
                    console.log("Error while saving")
                }, goalType);
                $.mobile.changePage('goal_type_short_note.html');
            }
        });

        $(document).on('pagebeforeshow', '#goal-type-desc', function (e) {
            var goalType = app.goalType;
            console.log("Goal type", app.goalType);
            if (!goalType) {
                $.mobile.back();
                return;
            }

            var img = 'img/default-image.jpg';

            if (goalType === 'entrepreneurship') {
                img = 'img/icon-bulb.png';
            } else if (goalType === 'employability') {
                img = 'img/icon-briefcase.png';
            } else if (goalType === 'personal') {
                img = 'img/icon-heart.png';
            }

            $("#goal-img").attr('src', img);
            $("#goal-title").html(goalType.capitalize());
            $("#goal-type").html(goalType.capitalize());
            $("#month").html(app.selectedMonth);
        });

        $(document).on('pagebeforeshow', '#goal-type-short-note', function (e) {
            var goalType = app.goalType;
            if (!goalType) {
                $.mobile.back();
                return;
            }

            var img = 'img/default-image.jpg';
            var example = "";

            if (goalType === 'entrepreneurship') {
                img = 'img/icon-bulb.png';
                example = "I'm saving towards starting a small side hustle";
            } else if (goalType === 'employability') {
                img = 'img/icon-briefcase.png';
                example = "I'm saving towards buying myself a laptop.";
            } else if (goalType === 'personal') {
                img = 'img/icon-heart.png';
                example = "I'm saving towards learning a technical skill"
            }

            $("#n_goal-img").attr('src', img);
            $("#n_goal-title").html(goalType.capitalize());
            $("#n_goal-type").html(goalType.capitalize());
            $("#n_month").html(app.selectedMonth);
            $("#n_goal-example").html(example);
        });

        $(document).on('pagebeforeshow', '#contribution', function (e) {
            var image = $("#c_goal-img");
            var goal = $("#c_goal-title");
            var week = $("#c_week");
            var button = $("#c_contributions-goal-vehicle");

            var goalType = app.goalType;

            var img = 'img/default-image.jpg';
            var example = "";

            if (goalType === 'entrepreneurship') {
                img = 'img/icon-bulb.png';
            } else if (goalType === 'employability') {
                img = 'img/icon-briefcase.png';
            } else if (goalType === 'personal') {
                img = 'img/icon-heart.png';
            }

            image.attr("src", img);
            goal.html(goalType.capitalize());
            week.html(app.getWeekString());

            button.click(function (e) {
                e.preventDefault();
                e.stopPropagation();
                var amount = $("[name=contribution_amount]").val();
                try {
                    amount = parseFloat(amount);

                    if ((amount <= 0 || amount > app.balance) && app.groupType !== 'champion') {
                        window.alert("Invalid contribution amount. Please note you can contribute upto " + app.balance);
                        return;
                    } else {
                        app.justSavedAmount = amount;
                        $.mobile.changePage('contribution_vehicle.html');
                    }

                } catch (e) {
                    window.alert("Invalid input. Please try again");
                }
            });
        });

        $(document).on('pagebeforeshow', '#goal_amount_selection', function (e) {
            $(document).find('.select-amount-goal-type').html(app.goalType.capitalize());
        });

        $(document).on('click', '[data-role=a-group-selection]', function (e) {
            var element = $(e.target).closest('a');

            var group = element.attr('data-group');
            if (!group) return;
            app.groupType = group;
            window._es.saveGroupType(null, null, group);
            $.mobile.changePage('goal_amount_selection_complete.html');
        });

        $(document).on('pagebeforeshow', '#goal_amount_selection_complete', function (e) {

            var goalType = app.goalType;
            var group = app.groupType;
            var amount = 0;

            var img = 'img/default-image.jpg';
            if (group === 'savvy') {
                img = 'img/icon-money-bag-smallest.png';
                amount = 500;
            } else if (group === 'power') {
                img = 'img/icon-money-bag-small.png';
                amount = 1000;
            } else if (group === 'super') {
                img = 'img/icon-money-bag-big.png';
                amount = 2000;
            } else if (group === 'champion') {
                img = 'img/icon-money-bag-biggest.png';
                amount = 2000;
            }
            $('#img-gasc').attr('src', img);
            $('#goal-gasc').html(goalType.capitalize());
            $('#group-gasc').html(group.capitalize());
            $("#gasc-amount").html(group === 'champion' ? "more than Naira  " + numberWithCommas(amount) : "Naira " + numberWithCommas(amount));
        });

        $(document).on('pagebeforeshow', '#group-stats', function (e) {


        });

        $(document).on('pagebeforeshow', '#contribution-success', function (e) {

            var goalType = app.goalType;
            var group = app.groupType;
            var amount = 0;

            var img = 'img/default-image.jpg';
            if (group === 'savvy') {
                img = 'img/icon-money-bag-smallest.png';
                amount = 500;
            } else if (group === 'power') {
                img = 'img/icon-money-bag-small.png';
                amount = 1000;
            } else if (group === 'super') {
                img = 'img/icon-money-bag-big.png';
                amount = 2000;
            } else if (group === 'champion') {
                img = 'img/icon-money-bag-biggest.png';
                amount = 2000;
            }
            $("#ss_goal-img").attr('src', img);
            $("#ss_goal_type").html(goalType.capitalize());
            $("#ss_amount").html(app.justSavedAmount);
        });

        $(document).on('change', 'input[name=month]', function (e) {

            var input = $(e.target);
            app.selectedMonth = input.val();
        });

        $(document).on('click', '#login-button', function (e) {
            e.preventDefault();
            var phoneNumber = $("#phone_number").val();
            if (!phoneNumber || !/\d+/.test(phoneNumber)) {
                window.alert("Invalid Phone Number. Please try again");
                return;
            }
            app.surveyId = phoneNumber;
            $.mobile.changePage('homepage.html');
        });

        $(document).on('click', '[data-role=btn-upload-data]', function () {
            app.save();
        });

        app.appStartTimeStamp = app.getTimeStamp();
        app.currentPageTimestamp = app.appStartTimeStamp;

        document.addEventListener("pause", function () {
            app.save();
        }, false);

        $(document).on("pagecreate", function () {
            window._es.getGoals(function (goals) {
                console.log("GOALS", goals);
                app.goals = _.map(goals, function(goal) {
                    var img = 'img/default-image.jpg';

                    if (goal.goalType === 'entrepreneurship') {
                        img = 'img/icon-bulb.png';
                    } else if (goal.goalType === 'employability') {
                        img = 'img/icon-briefcase.png';
                    } else if (goal.goalType === 'personal') {
                        img = 'img/icon-heart.png';
                    }

                    if (goal.groupType === "champion") {
                        goal.goalMax = "-"
                    }
                    goal.img = img;
                    goal.timestamp = moment(goal.timestamp).format("MMM Do, hh:mma");
                    return goal;
                });
                app.showCurrentGoals = goals.length > 0;

                app.initBindings();
            }, function () {
            });
        });

        $(document).on("pagecontainerload", function (event, data) {

            var pageAnalytics = {};

            pageAnalytics.timeStamp = app.getTimeStamp();
            pageAnalytics.timeSpent = 0; //app.timeTracker(Date.now());

            if (app.dataStore.length > 0) {
                app.dataStore[app.dataStore.length - 1].timeSpent = app.timeTracker(Date.now());

                if (window._es) {
                    var record = JSON.stringify(app.dataStore[app.dataStore.length - 1]);
                    window._es.addRecord(function () {
                        console.log(arguments);
                    }, function () {
                        console.log(arguments);
                    }, record);
                }
            }

            var absUrl = data.absUrl;
            var thePreviousPage = app.prevPage;
            var currentPage = app.util.getPageTitleFromUrl(absUrl);

            app.updateSessionVars(currentPage);

            if (currentPage === 'goal_type_desc' || currentPage === 'goal_type_short_note') currentPage = app.goalType + "_" + currentPage;
            else if (currentPage === 'goal_amount_selection') currentPage = app.goalType + "_" + currentPage;
            else if (currentPage === 'goal_amount_selection_complete') currentPage = app.groupType + "_" + app.goalType + "_" + currentPage;
            else if (currentPage === 'group_stats') currentPage = app.groupType + "_" + app.goalType + "_" + currentPage;

            console.log("Current Page", currentPage);

            pageAnalytics.previousPage = thePreviousPage;
            pageAnalytics.pageName = currentPage;

            pageAnalytics.pageOrder = app.pageOrder;
            app.pageOrder++;
            app.prevPage = currentPage;
            var inputTxt = $(data.page).find("input");
            var isInputPresent = inputTxt.length ? "yes" : "no";
            pageAnalytics.isInputPresent = isInputPresent;
            pageAnalytics.inputStats = null;

            var inputFieldAnalytics = null;

            if (inputTxt.length) {
                var backspaceCount = 0;
                var totalKeyPressCount = 0;
                var timeStartTyping = 0;
                var timeStopTyping = 0;
                var timeSpentInField = 0;
                var finalInputValue = void 0;
                var finalInputLength = void 0;
                var intelliWordChanges = [""];
                var intelliWordIndex = 0;
                var inputStream = "";

                inputTxt.change(function (e) {

                    var name = inputTxt.attr("name");
                    var type = inputTxt.attr('type');

                    finalInputValue = type === 'radio' ? $("input[name=" + name + "]:checked").val() : inputTxt.val();

                    if (type === 'checkbox') {
                        var checkedVals = _.map($("input[name=" + name + "]:checked"), function (item) {
                            return $(item).val();
                        });
                        console.log("CHECKED_VALUES", checkedVals);
                        finalInputValue = _.join(checkedVals, ",");
                    }

                    console.log("Final input value", finalInputValue, type, $("input[name=" + name + "]:checked").val());
                    if (!finalInputValue) return;

                    finalInputLength = finalInputValue.length;
                    timeStopTyping = app.getTimeStamp();
                    timeSpentInField = moment.duration(moment(timeStopTyping).diff(moment(timeStartTyping)))._milliseconds;
                    intelliWordChanges.shift();
                    var inputStatistics = {
                        backspaceCount: backspaceCount,
                        totalKeyPressCount: totalKeyPressCount,
                        timeStartTyping: timeStartTyping,
                        timeStopTyping: timeStopTyping,
                        timeSpentInField: timeSpentInField,
                        finalInputValue: finalInputValue,
                        finalInputLength: finalInputLength,
                        intelliWordChanges: intelliWordChanges.toString(),
                        intelliWordIndex: intelliWordIndex,
                        name: inputTxt.attr("name")

                    };
                    pageAnalytics.inputStats = inputStatistics;
                    app.populateDs(pageAnalytics);
                }).keyup(function (e) {
                    totalKeyPressCount++;
                    if (e.keyCode !== 8 || e.keyCode !== 46) {
                        inputStream += e.key;
                    }

                    if (e.keyCode === 8 || e.keyCode === 46) {
                        backspaceCount++;
                        if (inputTxt.val().length === 1) {
                            intelliWordIndex++;
                            intelliWordChanges[intelliWordIndex] = inputStream;
                            inputStream = "";
                        }
                    }
                    if (inputTxt.val().length === 1 && timeStartTyping !== 0) {
                        timeStartTyping = app.getTimeStamp();
                    }
                }).focus(function (e) {
                    // console.log(e.timeStamp);
                });

                var radioButtons = $(data.page).find("input").toArray().filter(function (input) {
                    return $(input).attr('type') === 'radio';
                });

                if (radioButtons.length > 0) {
                    var mth = currentMonth();
                    radioButtons.forEach(function (button) {
                        var b = $(button);
                        b.attr('disabled', true);
                        if (b.val() === mth) {
                            b.prop('checked', true);
                            setTimeout(function () {
                                b.change();
                            }, 50);
                        }
                    });
                }
            } else {
                app.populateDs(pageAnalytics);
            }
        });
        $(document).on("click", "#survey-complete", function () {

            app.save();
        });

        $(document).on("click", "#survey-id-submit", function () {

            var x = $("#survey-id").val();

            if (x !== "") {

                app.surveyId = x;
                $('[data-role=dialog]').dialog("close");
            }
        });
    },
    // Update DOM on a Received Event
    receivedEvent: function receivedEvent(id) {
        var parentElement = document.getElementById(id);
        var listeningElement = parentElement.querySelector('.listening');
        var receivedElement = parentElement.querySelector('.received');

        listeningElement.setAttribute('style', 'display:none;');
        receivedElement.setAttribute('style', 'display:block;');

        console.log('Received Event: ' + id);
    },

    getTimeStamp: function getTimeStamp() {

        return Date.now();
    },

    save: function save() {

        /*$.mobile.loading('show', {
            text: 'Saving',
            theme: 'z',
            textVisible: true
        });
        $.ajax({
            type: "POST",
            url: "http://34.211.227.26/busara.php",
            data: {
                analytics: app.dataStore.slice(0, app.dataStore.length - 1),
                application: 'npower',
                user: app.surveyId
            },
            dataType: "text",
            error: function error() {
                //$('.ui-loader').hide();
                $.mobile.loading('hide');
                //TODO logic  to resend request
                window.alert("An error has occured. Please check your internet connection and try again");
            },
            success: function success(data) {
                //$('.ui-loader').hide();
                $.mobile.loading('hide');
                if (data) {
                    window.alert("An error has occurred. Please try again");
                    console.error(data);
                } else {
                    app.dataStore.splice(0, app.dataStore.length - 1);
                    window.alert("Saved successfully");
                }
            }
        });*/
    }
};

app.initialize();