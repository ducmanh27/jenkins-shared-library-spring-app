import com.jenkins.helpers.NotifyHelper

def call(String status) {
    def helper = new NotifyHelper(this)
    helper.notify(status)
}
