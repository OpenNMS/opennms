describe('Requisitions Experiments', function() {
  it('retrieve requisitions using angular service', function() {
    browser.get('http://localhost:8980/opennms/');

    var loginButton = by.css('btn btn-default');
    browser.driver.wait(function() {
        return browser.driver.isElementPresent(loginButton);
    }, 5000);

    // Authenticate
    element(by.id('input_j_username')).sendKeys('admin');
    element(by.id('input_j_password')).sendKeys('admin');
    element(loginButton).click();

    expect(element(by.linkText('Log Out'))).not.toBe(null);

    browser.get('http://admin:admin@localhost:8980/opennms/admin/ng-requisitions/app/index.jsp');

  });
});