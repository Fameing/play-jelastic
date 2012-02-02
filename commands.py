# Here you can create play commands that are specific to the module, and extend existing commands

import sys
import os
import shutil
import subprocess
import tempfile
from optparse import OptionParser

try:
    from play.utils import package_as_war

    PLAY10 = False
except ImportError:
    PLAY10 = True

MODULE = 'jelastic'

# Commands that are specific to your module

COMMANDS = ['jelastic:deploy','jelastic:publish']
java_args = []
war_path = None
message = []


class MyOptionParser(OptionParser):
    def error(self, msg):
        pass


def execute(**kargs):
    command = kargs.get("command")
    app = kargs.get("app")
    args = kargs.get("args")
    env = kargs.get("env")
    parser = MyOptionParser()
    parser.add_option("-l", "--login", dest="login", help="Your login")
    parser.add_option("-p", "--password", dest="password", help="Your password")
    parser.add_option("-c", "--context", dest = "context", help="Deploy context")
    parser.add_option("-e", "--environment", dest = "environment", help="Environment name")
    parser.add_option("-a", "--apihoster", dest = "apihoster", help="Url to api hoster")
    options, args = parser.parse_args(args)
    app.check()
    war_path = None
    java_args = []

    jelastic_command = command[command.index(":") + 1:]

    for item in ["login", "password", "context", "environment", "apihoster"]:
        if eval('options.%s' % item) is not None:
            java_args.append("-Djelastic.api.%s=%s" % (item, eval('options.%s' % item)))
            print java_args

    if "deploy" in jelastic_command:
        war_path =  generate_war(app, env, args)
        java_args.append("-Djelastic.app.war=%s" % war_path)
        java_cmd = app.java_cmd(java_args, None, "play.modules.jelastic.Jelastic", [jelastic_command])
        try:
            subprocess.call(java_cmd, env=os.environ)
        except OSError:
            print "Could not execute the java executable, please make sure the JAVA_HOME environment variable is set properly (the java executable should reside at JAVA_HOME/bin/java). "
            sys.exit(-1)
        print

    if "publish" in jelastic_command:
        war_path =  generate_war(app, env, args)
        java_args.append("-Djelastic.app.war=%s" % war_path)

        java_cmd = app.java_cmd(java_args, None, "play.modules.jelastic.Jelastic", [jelastic_command])
        try:
            subprocess.call(java_cmd, env=os.environ)
        except OSError:
            print "Could not execute the java executable, please make sure the JAVA_HOME environment variable is set properly (the java executable should reside at JAVA_HOME/bin/java). "
            sys.exit(-1)
        print


def generate_war(app, env, args):
    java_cmd = app.java_cmd(args)
    if os.path.exists(os.path.join(app.path, 'tmp')):
        shutil.rmtree(os.path.join(app.path, 'tmp'))
    if os.path.exists(os.path.join(app.path, 'precompiled')):
        shutil.rmtree(os.path.join(app.path, 'precompiled'))
    java_cmd.insert(2, '-Dprecompile=yes')
    try:
        result = subprocess.call(java_cmd, env=os.environ)
        if not result == 0:
            print "~"
            print "~ Precompilation has failed, stop deploying."
            print "~"
            sys.exit(-1)

    except OSError:
        print "Could not execute the java executable, please make sure the JAVA_HOME environment variable is set properly (the java executable should reside at JAVA_HOME/bin/java). "
        sys.exit(-1)

    war_path = os.path.join(tempfile.gettempdir(), os.path.basename(app.path))
    package_as_war(app, env, war_path, "%s.war" % war_path)
    return "%s.war" % war_path

# This will be executed before any command (new, run...)
def before(**kargs):
    command = kargs.get("command")
    app = kargs.get("app")
    args = kargs.get("args")
    env = kargs.get("env")


# This will be executed after any command (new, run...)
def after(**kargs):
    command = kargs.get("command")
    app = kargs.get("app")
    args = kargs.get("args")
    env = kargs.get("env")

    if command == "new":
        appconf = open(os.path.join(app.path, 'conf/application.conf'), 'a')
        appconf.write("\n")
        appconf.write("# Jelastic Account configuration\n")
        appconf.write("# ~~~~~\n")
        appconf.write("# %jelastic.api.login=\n")
        appconf.write("# %jelastic.api.password=\n")
        appconf.write("# %jelastic.api.context=\n")
        appconf.write("# %jelastic.api.environment=\n")
        appconf.write("# %jelastic.api.apihoster=\n")
